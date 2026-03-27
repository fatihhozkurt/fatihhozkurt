#!/usr/bin/env node

const baseUrl = process.env.APP_BASE_URL ?? 'http://localhost'
const locale = process.env.APP_LOCALE ?? 'en'
const adminUsername = process.env.ADMIN_USERNAME ?? 'antesionn'
const adminPassword = process.env.ADMIN_PASSWORD ?? '18668798Fö.'

const cookieJar = new Map()
let accessToken = null
let csrfHeaderName = null
let csrfToken = null

function putCookie(setCookieHeader) {
  if (!setCookieHeader) {
    return
  }

  const [pair] = setCookieHeader.split(';')
  const separatorIndex = pair.indexOf('=')
  if (separatorIndex < 1) {
    return
  }

  const key = pair.slice(0, separatorIndex).trim()
  const value = pair.slice(separatorIndex + 1).trim()
  cookieJar.set(key, value)
}

function splitSetCookieHeader(value) {
  if (!value) {
    return []
  }
  return value.split(/,(?=\s*[A-Za-z0-9_\-]+=)/)
}

function updateCookies(response) {
  const setCookies = typeof response.headers.getSetCookie === 'function'
    ? response.headers.getSetCookie()
    : splitSetCookieHeader(response.headers.get('set-cookie'))
  const fallbackCookies = setCookies.length > 0
    ? setCookies
    : []
  fallbackCookies.forEach(putCookie)
}

function cookieHeader() {
  if (cookieJar.size === 0) {
    return null
  }
  return [...cookieJar.entries()].map(([key, value]) => `${key}=${value}`).join('; ')
}

async function refreshCsrfToken() {
  const headers = {
    Accept: 'application/json',
    'X-Locale': locale,
  }
  const cookie = cookieHeader()
  if (cookie) {
    headers.Cookie = cookie
  }

  const response = await fetch(`${baseUrl}/api/v1/auth/csrf`, {
    method: 'GET',
    headers,
  })
  updateCookies(response)

  const text = await response.text()
  const payload = text ? JSON.parse(text) : null
  if (!response.ok) {
    throw new Error(`GET /api/v1/auth/csrf failed (${response.status}): ${text}`)
  }

  csrfHeaderName = payload?.headerName
  csrfToken = payload?.token
  if (!csrfHeaderName || !csrfToken) {
    throw new Error('CSRF payload should include headerName and token')
  }
}

async function request(path, { method = 'GET', body, auth = false, csrf = false } = {}) {
  const headers = {
    Accept: 'application/json',
    'X-Locale': locale,
  }

  if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }

  if (auth) {
    if (!accessToken) {
      throw new Error(`Missing access token for ${method} ${path}`)
    }
    headers.Authorization = `Bearer ${accessToken}`
  }

  if (csrf) {
    await refreshCsrfToken()
    headers[csrfHeaderName] = csrfToken
  }

  const cookie = cookieHeader()
  if (cookie) {
    headers.Cookie = cookie
  }

  const response = await fetch(`${baseUrl}${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  })

  updateCookies(response)

  const text = await response.text()
  const payload = text ? JSON.parse(text) : null

  if (!response.ok) {
    throw new Error(`${method} ${path} failed (${response.status}): ${text}`)
  }

  return payload
}

function assert(condition, message) {
  if (!condition) {
    throw new Error(message)
  }
}

function logStep(message) {
  console.log(`- ${message}`)
}

async function main() {
  logStep('Public endpoints respond')
  const publicHero = await request('/api/v1/public/hero')
  const publicProjects = await request('/api/v1/public/projects')
  assert(Array.isArray(publicProjects), 'Public projects should be an array')
  assert(publicHero?.fullName, 'Public hero should include fullName')

  logStep('Admin login works with configured credentials')
  const loginResponse = await request('/api/v1/auth/login', {
    method: 'POST',
    body: {
      username: adminUsername,
      password: adminPassword,
    },
  })
  accessToken = loginResponse.accessToken
  assert(accessToken, 'Login should return access token')

  logStep('Refresh token flow returns a new access token')
  const refreshResponse = await request('/api/v1/auth/refresh', {
    method: 'POST',
  })
  accessToken = refreshResponse.accessToken
  assert(accessToken, 'Refresh should return access token')

  logStep('CSRF token endpoint returns active token')
  await refreshCsrfToken()

  logStep('Admin overview endpoints respond with real data shape')
  const dashboardOverview = await request('/api/v1/admin/dashboard/overview', { auth: true })
  const analyticsOverview = await request('/api/v1/admin/analytics/overview', { auth: true })
  const countriesBeforeVisit = await request('/api/v1/admin/analytics/country-distribution', { auth: true })
  const trendBeforeVisit = await request('/api/v1/admin/analytics/visits-trend?days=7', { auth: true })
  assert(typeof dashboardOverview?.visitsToday === 'number', 'Dashboard overview should contain visitsToday')
  assert(typeof analyticsOverview?.visitsToday === 'number', 'Analytics overview should contain visitsToday')
  assert(Array.isArray(countriesBeforeVisit), 'Country distribution should be an array')
  assert(Array.isArray(trendBeforeVisit), 'Visit trend should be an array')

  logStep('Public visit tracking updates analytics')
  await request('/api/v1/public/visits', {
    method: 'POST',
    body: {
      path: '/projects',
      country: 'Turkiye',
    },
  })
  const countriesAfterVisit = await request('/api/v1/admin/analytics/country-distribution', { auth: true })
  assert(countriesAfterVisit.length >= 1, 'Country distribution should include at least one row after visit')

  logStep('Hero content update + public reflection works')
  const heroBefore = await request('/api/v1/admin/content/hero', { auth: true })
  const heroUpdated = await request('/api/v1/admin/content/hero', {
    method: 'PUT',
    auth: true,
    csrf: true,
    body: {
      welcomeText: heroBefore.welcomeText,
      fullName: heroBefore.fullName,
      title: heroBefore.title,
      description: `${heroBefore.description} [verified]`,
      ctaLabel: heroBefore.ctaLabel,
    },
  })
  const publicHeroAfter = await request('/api/v1/public/hero')
  assert(publicHeroAfter.description === heroUpdated.description, 'Public hero should reflect admin update')

  logStep('About content update + public reflection works')
  const aboutBefore = await request('/api/v1/admin/content/about', { auth: true })
  const aboutUpdated = await request('/api/v1/admin/content/about', {
    method: 'PUT',
    auth: true,
    csrf: true,
    body: {
      eyebrow: aboutBefore.eyebrow,
      title: aboutBefore.title,
      description: `${aboutBefore.description} [verified]`,
    },
  })
  const publicAboutAfter = await request('/api/v1/public/about')
  assert(publicAboutAfter.description === aboutUpdated.description, 'Public about should reflect admin update')

  logStep('Contact profile update + public reflection works')
  const contactProfileBefore = await request('/api/v1/admin/contact-profile', { auth: true })
  const contactProfileUpdated = await request('/api/v1/admin/contact-profile', {
    method: 'PUT',
    auth: true,
    csrf: true,
    body: {
      email: contactProfileBefore.email,
      recipientEmail: contactProfileBefore.email,
      linkedinUrl: contactProfileBefore.linkedinUrl,
      githubUrl: contactProfileBefore.githubUrl,
      mediumUrl: contactProfileBefore.mediumUrl,
    },
  })
  const publicContactAfter = await request('/api/v1/public/contact-profile')
  assert(publicContactAfter.email === contactProfileUpdated.email, 'Public contact profile should reflect admin update')

  logStep('Tech stack CRUD works')
  const techCreated = await request('/api/v1/admin/tech-stack', {
    method: 'POST',
    auth: true,
    csrf: true,
    body: {
      name: 'Kotlin',
      iconName: 'Kotlin',
      category: 'backend',
      sortOrder: 999,
      active: true,
    },
  })
  const techUpdated = await request(`/api/v1/admin/tech-stack/${techCreated.id}`, {
    method: 'PUT',
    auth: true,
    csrf: true,
    body: {
      name: 'Kotlin JVM',
      iconName: 'Kotlin',
      category: 'backend',
      sortOrder: 998,
      active: true,
    },
  })
  assert(techUpdated.name === 'Kotlin JVM', 'Tech update should return updated name')
  await request(`/api/v1/admin/tech-stack/${techCreated.id}`, {
    method: 'DELETE',
    auth: true,
    csrf: true,
  })

  logStep('Project CRUD works')
  const projectCreated = await request('/api/v1/admin/projects', {
    method: 'POST',
    auth: true,
    csrf: true,
    body: {
      title: 'Integration Test Project',
      category: 'Backend Validation',
      summary: 'Created by verification flow.',
      repositoryUrl: 'https://github.com/fatihozkurt',
      demoUrl: null,
      readmeMarkdown: '## Verification\n- CRUD validated',
      coverImageUrl: 'https://example.com/project-surface.svg',
      stackCsv: 'Java,Spring Boot',
      sortOrder: 987,
      active: true,
    },
  })
  const projectUpdated = await request(`/api/v1/admin/projects/${projectCreated.id}`, {
    method: 'PUT',
    auth: true,
    csrf: true,
    body: {
      title: 'Integration Test Project Updated',
      category: 'Backend Validation',
      summary: 'Updated by verification flow.',
      repositoryUrl: 'https://github.com/fatihozkurt',
      demoUrl: null,
      readmeMarkdown: '## Verification\n- CRUD updated',
      coverImageUrl: 'https://example.com/project-surface.svg',
      stackCsv: 'Java,Spring Boot,PostgreSQL',
      sortOrder: 986,
      active: true,
    },
  })
  assert(projectUpdated.title === 'Integration Test Project Updated', 'Project update should return updated title')
  await request(`/api/v1/admin/projects/${projectCreated.id}`, {
    method: 'DELETE',
    auth: true,
    csrf: true,
  })

  logStep('Article CRUD works')
  const articleCreated = await request('/api/v1/admin/articles', {
    method: 'POST',
    auth: true,
    csrf: true,
    body: {
      title: 'Integration Test Article',
      excerpt: 'Created by verification flow.',
      href: 'https://medium.com/@fatihozkurt',
      readingTime: '3 min read',
      publishedAt: '2026-03-26',
      sortOrder: 975,
      active: true,
    },
  })
  const articleUpdated = await request(`/api/v1/admin/articles/${articleCreated.id}`, {
    method: 'PUT',
    auth: true,
    csrf: true,
    body: {
      title: 'Integration Test Article Updated',
      excerpt: 'Updated by verification flow.',
      href: 'https://medium.com/@fatihozkurt',
      readingTime: '4 min read',
      publishedAt: '2026-03-26',
      sortOrder: 974,
      active: true,
    },
  })
  assert(articleUpdated.title === 'Integration Test Article Updated', 'Article update should return updated title')
  await request(`/api/v1/admin/articles/${articleCreated.id}`, {
    method: 'DELETE',
    auth: true,
    csrf: true,
  })

  logStep('Resume replacement works')
  const resumeUpdated = await request('/api/v1/admin/resume/replace', {
    method: 'POST',
    auth: true,
    csrf: true,
    body: {
      fileName: 'fatih-ozkurt-cv-v2.pdf',
      objectKey: 'defaults/fatih-ozkurt-cv.pdf',
      contentType: 'application/pdf',
      sizeBytes: 204800,
    },
  })
  assert(resumeUpdated.fileName === 'fatih-ozkurt-cv-v2.pdf', 'Resume replacement should return new file name')

  logStep('Resume upload endpoint accepts multipart and updates active document')
  const uploadForm = new FormData()
  uploadForm.append(
    'file',
    new Blob([Buffer.from('%PDF-1.4\\nresume upload test\\n%%EOF', 'utf-8')], { type: 'application/pdf' }),
    'resume-upload-test.pdf'
  )
  await refreshCsrfToken()
  const uploadHeaders = {
    Accept: 'application/json',
    Authorization: `Bearer ${accessToken}`,
    [csrfHeaderName]: csrfToken,
    'X-Locale': locale,
  }
  const cookie = cookieHeader()
  if (cookie) {
    uploadHeaders.Cookie = cookie
  }
  const uploadResponse = await fetch(`${baseUrl}/api/v1/admin/resume/upload`, {
    method: 'POST',
    headers: uploadHeaders,
    body: uploadForm,
  })
  updateCookies(uploadResponse)
  const uploadText = await uploadResponse.text()
  if (!uploadResponse.ok) {
    throw new Error(`POST /api/v1/admin/resume/upload failed (${uploadResponse.status}): ${uploadText}`)
  }
  const uploadedResume = uploadText ? JSON.parse(uploadText) : null
  assert(uploadedResume?.fileName === 'resume-upload-test.pdf', 'Resume upload should set uploaded file as active')

  const resumeDownload = await fetch(`${baseUrl}/api/v1/public/resume/download`, {
    method: 'GET',
    headers: {
      Accept: 'application/pdf',
      'X-Locale': locale,
    },
  })
  assert(resumeDownload.ok, `Resume download should succeed but failed with ${resumeDownload.status}`)

  logStep('Contact message submission is visible from admin panel feed')
  await request('/api/v1/public/contact-messages', {
    method: 'POST',
    body: {
      title: 'Verification',
      email: 'qa@example.com',
      content: 'This message is sent by the verification flow.',
    },
  })
  const adminContactMessages = await request('/api/v1/admin/contact-messages', { auth: true })
  assert(adminContactMessages.length >= 1, 'Admin contact message feed should include at least one record')

  logStep('Forgot-password delivery hook works (non-destructive)')
  try {
    await request('/api/v1/auth/forgot-password', {
      method: 'POST',
      body: {
        email: 'fatih@example.com',
      },
    })
  } catch (error) {
    const message = String(error?.message ?? '')
    if (!message.includes('POST /api/v1/auth/forgot-password failed (429):')) {
      throw error
    }
    logStep('Forgot-password rate limit is active (expected protection)')
  }

  logStep('Logout works')
  await request('/api/v1/auth/logout', {
    method: 'POST',
    auth: true,
    csrf: true,
  })

  console.log('\nVerification completed successfully.')
}

main().catch((error) => {
  console.error('\nVerification failed:')
  console.error(error.message)
  process.exit(1)
})
