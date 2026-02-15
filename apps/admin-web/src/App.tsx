import './App.css'

import { useCallback, useMemo, useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'

type Role = 'BUYER' | 'SELLER' | 'BROKER' | 'ADMIN'

type CategoryAttributeDefinition = {
  key: string
  type: string
  required: boolean
  enumValues: string[] | null
  unit: string | null
  filterable: boolean
}

type Category = {
  id: string
  name: string
  attributes: CategoryAttributeDefinition[]
}

type LoginResponse = {
  accessToken: string
  refreshToken: string
  userId: string
  companyId: string
  role: Role
}

type CompanyApprovalResponse = {
  companyId: string
  approved: boolean
}

type BrokerInterventionResponse = {
  rfqId: string
  brokerUserId: string
  note: string
  createdAtEpochMs: number
}

type ProductDraftResponse = {
  draftId: string
  categoryId: string
  sellerUserId: string
  sourceFileName: string
  parsedFields: Record<string, string>
  confidence: Record<string, number>
  status: string
  parseJobId: string | null
  lastError: string | null
}

type ProductResponse = {
  productId: string
  categoryId: string
  sellerCompanyId: string
  attributes: Record<string, string>
  active: boolean
}

type ViewMode = 'ADMIN' | 'BROKER' | 'SELLER'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
const typeOptions = ['STRING', 'NUMBER', 'BOOLEAN', 'ENUM', 'DATE']

function parseEnumValues(raw: string): string[] | null {
  const values = raw
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
  return values.length > 0 ? values : null
}

async function readError(response: Response, fallback: string): Promise<Error> {
  const text = (await response.text()).trim()
  if (!text) {
    return new Error(`${fallback}: ${response.status}`)
  }
  return new Error(`${fallback}: ${response.status} ${text}`)
}

function App() {
  const [viewMode, setViewMode] = useState<ViewMode>('ADMIN')

  const [token, setToken] = useState<string | null>(null)
  const [role, setRole] = useState<Role | null>(null)
  const [message, setMessage] = useState('Hazir')
  const [loading, setLoading] = useState(false)
  const [categories, setCategories] = useState<Category[]>([])
  const [selectedCategoryId, setSelectedCategoryId] = useState('')

  const [email, setEmail] = useState('admin@tradebridge.local')
  const [password, setPassword] = useState('admin123')
  const [categoryName, setCategoryName] = useState('')
  const [attributeKey, setAttributeKey] = useState('')
  const [attributeType, setAttributeType] = useState('STRING')
  const [attributeRequired, setAttributeRequired] = useState(true)
  const [attributeFilterable, setAttributeFilterable] = useState(true)
  const [attributeUnit, setAttributeUnit] = useState('')
  const [attributeEnumValues, setAttributeEnumValues] = useState('')

  const [brokerEmail, setBrokerEmail] = useState('')
  const [brokerPassword, setBrokerPassword] = useState('broker123')
  const [brokerToken, setBrokerToken] = useState<string | null>(null)
  const [rfqId, setRfqId] = useState('')
  const [brokerNote, setBrokerNote] = useState('Iletisim hizlandirma notu')
  const [brokerResult, setBrokerResult] = useState<BrokerInterventionResponse | null>(null)

  const [sellerEmail, setSellerEmail] = useState('')
  const [sellerPassword, setSellerPassword] = useState('seller123')
  const [sellerToken, setSellerToken] = useState<string | null>(null)
  const [sellerFile, setSellerFile] = useState<File | null>(null)
  const [sellerDraft, setSellerDraft] = useState<ProductDraftResponse | null>(null)
  const [sellerProduct, setSellerProduct] = useState<ProductResponse | null>(null)

  const selectedCategory = useMemo(
    () => categories.find((item) => item.id === selectedCategoryId) ?? null,
    [categories, selectedCategoryId],
  )

  const login = useCallback(async (userEmail: string, userPassword: string) => {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: userEmail, password: userPassword }),
    })
    if (!response.ok) {
      throw await readError(response, 'Login basarisiz')
    }
    return (await response.json()) as LoginResponse
  }, [])

  const fetchCategories = useCallback(
    async (accessToken: string) => {
      const response = await fetch(`${API_BASE_URL}/categories`, {
        headers: { Authorization: `Bearer ${accessToken}` },
      })

      if (!response.ok) {
        throw await readError(response, 'Kategori listesi alinamadi')
      }

      const data = (await response.json()) as Category[]
      setCategories(data)
      if (!selectedCategoryId && data.length > 0) {
        setSelectedCategoryId(data[0].id)
      }
      return data
    },
    [selectedCategoryId],
  )

  const handleAdminLogin = useCallback(
    async (event: FormEvent<HTMLFormElement>) => {
      event.preventDefault()
      setLoading(true)
      setMessage('Login deneniyor...')
      try {
        const data = await login(email, password)
        setToken(data.accessToken)
        setRole(data.role)
        const listed = await fetchCategories(data.accessToken)
        setMessage(`Giris basarili. ${listed.length} kategori yuklendi.`)
      } catch (error) {
        const reason = error instanceof Error ? error.message : 'unknown'
        setMessage(`Hata: ${reason}`)
      } finally {
        setLoading(false)
      }
    },
    [email, password, fetchCategories, login],
  )

  const handleCreateCategory = useCallback(
    async (event: FormEvent<HTMLFormElement>) => {
      event.preventDefault()
      if (!token) {
        setMessage('Once admin login olmalisin.')
        return
      }
      setLoading(true)
      setMessage('Kategori olusturuluyor...')
      try {
        const response = await fetch(`${API_BASE_URL}/admin/categories`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({ name: categoryName }),
        })
        if (!response.ok) {
          throw await readError(response, 'Kategori olusturulamadi')
        }
        const created = (await response.json()) as Category
        const listed = await fetchCategories(token)
        setSelectedCategoryId(created.id)
        setCategoryName('')
        setMessage(`Kategori eklendi. Toplam ${listed.length} kategori var.`)
      } catch (error) {
        const reason = error instanceof Error ? error.message : 'unknown'
        setMessage(`Hata: ${reason}`)
      } finally {
        setLoading(false)
      }
    },
    [token, categoryName, fetchCategories],
  )

  const handleAddAttribute = useCallback(
    async (event: FormEvent<HTMLFormElement>) => {
      event.preventDefault()
      if (!token || !selectedCategoryId) {
        setMessage('Once login ol ve bir kategori sec.')
        return
      }
      setLoading(true)
      setMessage('Attribute ekleniyor...')
      try {
        const payload = {
          key: attributeKey,
          type: attributeType,
          required: attributeRequired,
          enumValues: attributeType === 'ENUM' ? parseEnumValues(attributeEnumValues) : null,
          unit: attributeUnit.trim() ? attributeUnit.trim() : null,
          filterable: attributeFilterable,
        }
        const response = await fetch(
          `${API_BASE_URL}/admin/categories/${selectedCategoryId}/attributes`,
          {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify(payload),
          },
        )
        if (!response.ok) {
          throw await readError(response, 'Attribute eklenemedi')
        }
        const listed = await fetchCategories(token)
        setAttributeKey('')
        setAttributeEnumValues('')
        setAttributeUnit('')
        setMessage(`Attribute eklendi. ${listed.length} kategori guncel.`)
      } catch (error) {
        const reason = error instanceof Error ? error.message : 'unknown'
        setMessage(`Hata: ${reason}`)
      } finally {
        setLoading(false)
      }
    },
    [
      token,
      selectedCategoryId,
      attributeKey,
      attributeType,
      attributeRequired,
      attributeEnumValues,
      attributeUnit,
      attributeFilterable,
      fetchCategories,
    ],
  )

  const setupDemoBroker = useCallback(async () => {
    setLoading(true)
    setMessage('Demo broker olusturuluyor...')
    try {
      const nonce = Date.now()
      const emailToUse = `broker.${nonce}@tradebridge.local`
      const companyName = `Broker Co ${nonce}`

      const registerResponse = await fetch(`${API_BASE_URL}/auth/register-company`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          companyName,
          email: emailToUse,
          password: brokerPassword,
          role: 'BROKER',
        }),
      })
      if (!registerResponse.ok) {
        throw await readError(registerResponse, 'Broker kaydi basarisiz')
      }
      const approval = (await registerResponse.json()) as CompanyApprovalResponse

      const admin = await login('admin@tradebridge.local', 'admin123')
      const approveResponse = await fetch(
        `${API_BASE_URL}/admin/companies/${approval.companyId}/approve`,
        {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${admin.accessToken}`,
          },
        },
      )
      if (!approveResponse.ok) {
        throw await readError(approveResponse, 'Broker sirket onayi basarisiz')
      }

      setBrokerEmail(emailToUse)
      setMessage(`Demo broker hazir: ${emailToUse}`)
    } catch (error) {
      const reason = error instanceof Error ? error.message : 'unknown'
      setMessage(`Hata: ${reason}`)
    } finally {
      setLoading(false)
    }
  }, [brokerPassword, login])

  const handleBrokerLogin = useCallback(
    async (event: FormEvent<HTMLFormElement>) => {
      event.preventDefault()
      setLoading(true)
      setMessage('Broker login deneniyor...')
      try {
        const data = await login(brokerEmail, brokerPassword)
        if (data.role !== 'BROKER' && data.role !== 'ADMIN') {
          throw new Error('Bu hesap broker degil')
        }
        setBrokerToken(data.accessToken)
        setMessage(`Broker login basarili (${data.role}).`)
      } catch (error) {
        const reason = error instanceof Error ? error.message : 'unknown'
        setMessage(`Hata: ${reason}`)
      } finally {
        setLoading(false)
      }
    },
    [brokerEmail, brokerPassword, login],
  )

  const handleBrokerIntervention = useCallback(
    async (event: FormEvent<HTMLFormElement>) => {
      event.preventDefault()
      if (!brokerToken) {
        setMessage('Once broker login olmalisin.')
        return
      }
      setLoading(true)
      setMessage('Broker mudahalesi gonderiliyor...')
      try {
        const response = await fetch(`${API_BASE_URL}/broker/rfqs/${rfqId}/interventions`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${brokerToken}`,
          },
          body: JSON.stringify({ note: brokerNote }),
        })
        if (!response.ok) {
          throw await readError(response, 'Mudahale gonderilemedi')
        }
        const result = (await response.json()) as BrokerInterventionResponse
        setBrokerResult(result)
        setMessage(`Mudahale kaydedildi. RFQ: ${result.rfqId}`)
      } catch (error) {
        const reason = error instanceof Error ? error.message : 'unknown'
        setMessage(`Hata: ${reason}`)
      } finally {
        setLoading(false)
      }
    },
    [brokerToken, rfqId, brokerNote],
  )

  const setupDemoSeller = useCallback(async () => {
    setLoading(true)
    setMessage('Demo seller olusturuluyor...')
    try {
      const nonce = Date.now()
      const emailToUse = `seller.${nonce}@tradebridge.local`
      const companyName = `Seller Co ${nonce}`

      const registerResponse = await fetch(`${API_BASE_URL}/auth/register-company`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          companyName,
          email: emailToUse,
          password: sellerPassword,
          role: 'SELLER',
        }),
      })
      if (!registerResponse.ok) {
        throw await readError(registerResponse, 'Seller kaydi basarisiz')
      }

      const approval = (await registerResponse.json()) as CompanyApprovalResponse
      const admin = await login('admin@tradebridge.local', 'admin123')
      const approveResponse = await fetch(
        `${API_BASE_URL}/admin/companies/${approval.companyId}/approve`,
        {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${admin.accessToken}`,
          },
        },
      )
      if (!approveResponse.ok) {
        throw await readError(approveResponse, 'Seller sirket onayi basarisiz')
      }

      setSellerEmail(emailToUse)
      setMessage(`Demo seller hazir: ${emailToUse}`)
    } catch (error) {
      const reason = error instanceof Error ? error.message : 'unknown'
      setMessage(`Hata: ${reason}`)
    } finally {
      setLoading(false)
    }
  }, [sellerPassword, login])

  const handleSellerLogin = useCallback(
    async (event: FormEvent<HTMLFormElement>) => {
      event.preventDefault()
      setLoading(true)
      setMessage('Seller login deneniyor...')
      try {
        const data = await login(sellerEmail, sellerPassword)
        if (data.role !== 'SELLER') {
          throw new Error('Bu hesap seller degil')
        }
        setSellerToken(data.accessToken)
        const listed = await fetchCategories(data.accessToken)
        setMessage(`Seller login basarili. ${listed.length} kategori yuklendi.`)
      } catch (error) {
        const reason = error instanceof Error ? error.message : 'unknown'
        setMessage(`Hata: ${reason}`)
      } finally {
        setLoading(false)
      }
    },
    [sellerEmail, sellerPassword, login, fetchCategories],
  )

  const handleSellerFile = useCallback((event: ChangeEvent<HTMLInputElement>) => {
    const next = event.target.files?.[0] ?? null
    setSellerFile(next)
    if (next) {
      setMessage(`Dosya secildi: ${next.name}`)
    }
  }, [])

  const handleSellerUpload = useCallback(
    async (event: FormEvent<HTMLFormElement>) => {
      event.preventDefault()
      if (!sellerToken) {
        setMessage('Once seller login olmalisin.')
        return
      }
      if (!selectedCategoryId) {
        setMessage('Kategori secmelisin.')
        return
      }
      if (!sellerFile) {
        setMessage('Dosya secmelisin.')
        return
      }

      setLoading(true)
      setMessage('Dosya yukleniyor...')
      try {
        const formData = new FormData()
        formData.append('categoryId', selectedCategoryId)
        formData.append('file', sellerFile)

        const response = await fetch(`${API_BASE_URL}/seller/products/drafts/upload`, {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${sellerToken}`,
          },
          body: formData,
        })
        if (!response.ok) {
          throw await readError(response, 'Draft upload basarisiz')
        }

        const draft = (await response.json()) as ProductDraftResponse
        setSellerDraft(draft)
        setSellerProduct(null)
        setMessage(`Draft olustu: ${draft.draftId}`)
      } catch (error) {
        const reason = error instanceof Error ? error.message : 'unknown'
        setMessage(`Hata: ${reason}`)
      } finally {
        setLoading(false)
      }
    },
    [sellerToken, selectedCategoryId, sellerFile],
  )

  const refreshSellerDraft = useCallback(async () => {
    if (!sellerToken || !sellerDraft) {
      setMessage('Refresh icin seller login ve draft gerekli.')
      return
    }

    setLoading(true)
    setMessage('Draft yenileniyor...')
    try {
      const response = await fetch(
        `${API_BASE_URL}/seller/products/drafts/${sellerDraft.draftId}`,
        {
          headers: {
            Authorization: `Bearer ${sellerToken}`,
          },
        },
      )
      if (!response.ok) {
        throw await readError(response, 'Draft yenilenemedi')
      }

      const draft = (await response.json()) as ProductDraftResponse
      setSellerDraft(draft)
      setMessage(`Draft guncel: ${draft.status}`)
    } catch (error) {
      const reason = error instanceof Error ? error.message : 'unknown'
      setMessage(`Hata: ${reason}`)
    } finally {
      setLoading(false)
    }
  }, [sellerToken, sellerDraft])

  const confirmSellerDraft = useCallback(async () => {
    if (!sellerToken || !sellerDraft) {
      setMessage('Confirm icin seller login ve draft gerekli.')
      return
    }

    setLoading(true)
    setMessage('Draft onaylaniyor...')
    try {
      const response = await fetch(
        `${API_BASE_URL}/seller/products/drafts/${sellerDraft.draftId}/confirm`,
        {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${sellerToken}`,
          },
        },
      )
      if (!response.ok) {
        throw await readError(response, 'Draft confirm basarisiz')
      }

      const product = (await response.json()) as ProductResponse
      setSellerProduct(product)
      setMessage(`Urun yayina alindi: ${product.productId}`)
      await refreshSellerDraft()
    } catch (error) {
      const reason = error instanceof Error ? error.message : 'unknown'
      setMessage(`Hata: ${reason}`)
    } finally {
      setLoading(false)
    }
  }, [sellerToken, sellerDraft, refreshSellerDraft])

  return (
    <main className="page">
      <header>
        <p className="eyebrow">trade-bridge admin + broker + seller</p>
        <h1>Yonetim ve RFQ Mudahale</h1>
        <p className="subtitle">
          API: <code>{API_BASE_URL}</code>
        </p>
        <div className="switchRow">
          <button
            type="button"
            className={viewMode === 'ADMIN' ? 'activeSwitch' : ''}
            onClick={() => setViewMode('ADMIN')}
          >
            ADMIN
          </button>
          <button
            type="button"
            className={viewMode === 'BROKER' ? 'activeSwitch' : ''}
            onClick={() => setViewMode('BROKER')}
          >
            BROKER
          </button>
          <button
            type="button"
            className={viewMode === 'SELLER' ? 'activeSwitch' : ''}
            onClick={() => setViewMode('SELLER')}
          >
            SELLER
          </button>
        </div>
      </header>

      {viewMode === 'ADMIN' && (
        <>
          <section className="grid two">
            <article className="card">
              <h2>1) Admin Login</h2>
              <form className="stack" onSubmit={handleAdminLogin}>
                <label>
                  Email
                  <input value={email} onChange={(event) => setEmail(event.target.value)} />
                </label>
                <label>
                  Sifre
                  <input
                    type="password"
                    value={password}
                    onChange={(event) => setPassword(event.target.value)}
                  />
                </label>
                <button type="submit" disabled={loading}>
                  {loading ? 'Calisiyor...' : 'Login Ol'}
                </button>
              </form>
              <p className="meta">Rol: {role ?? '-'}</p>
            </article>

            <article className="card">
              <h2>2) Kategori Olustur</h2>
              <form className="stack" onSubmit={handleCreateCategory}>
                <label>
                  Kategori Adi
                  <input
                    value={categoryName}
                    onChange={(event) => setCategoryName(event.target.value)}
                    placeholder="Orn: Baharat"
                    required
                  />
                </label>
                <button type="submit" disabled={loading || !token}>
                  Kategori Ekle
                </button>
              </form>
            </article>
          </section>

          <section className="grid two">
            <article className="card">
              <h2>3) Kategori Listesi</h2>
              <div className="stack">
                <button
                  type="button"
                  onClick={() => token && fetchCategories(token)}
                  disabled={!token || loading}
                >
                  Listeyi Yenile
                </button>
                <label>
                  Secili Kategori
                  <select
                    value={selectedCategoryId}
                    onChange={(event) => setSelectedCategoryId(event.target.value)}
                  >
                    <option value="">Kategori sec</option>
                    {categories.map((category) => (
                      <option key={category.id} value={category.id}>
                        {category.name}
                      </option>
                    ))}
                  </select>
                </label>
              </div>
              <ul className="list">
                {categories.map((category) => (
                  <li key={category.id}>
                    <strong>{category.name}</strong>
                    <span>{category.attributes.length} attribute</span>
                  </li>
                ))}
              </ul>
            </article>

            <article className="card">
              <h2>4) Attribute Ekle</h2>
              <form className="stack" onSubmit={handleAddAttribute}>
                <label>
                  Key
                  <input
                    value={attributeKey}
                    onChange={(event) => setAttributeKey(event.target.value)}
                    placeholder="orn: mensei"
                    required
                  />
                </label>

                <label>
                  Type
                  <select
                    value={attributeType}
                    onChange={(event) => setAttributeType(event.target.value)}
                  >
                    {typeOptions.map((type) => (
                      <option key={type} value={type}>
                        {type}
                      </option>
                    ))}
                  </select>
                </label>

                <label>
                  Unit
                  <input
                    value={attributeUnit}
                    onChange={(event) => setAttributeUnit(event.target.value)}
                    placeholder="orn: kg"
                  />
                </label>

                <label>
                  Enum Values (virgulle)
                  <input
                    value={attributeEnumValues}
                    onChange={(event) => setAttributeEnumValues(event.target.value)}
                    placeholder="orn: TR,IR,US"
                    disabled={attributeType !== 'ENUM'}
                  />
                </label>

                <label className="check">
                  <input
                    type="checkbox"
                    checked={attributeRequired}
                    onChange={(event) => setAttributeRequired(event.target.checked)}
                  />
                  Required
                </label>
                <label className="check">
                  <input
                    type="checkbox"
                    checked={attributeFilterable}
                    onChange={(event) => setAttributeFilterable(event.target.checked)}
                  />
                  Filterable
                </label>

                <button type="submit" disabled={loading || !token || !selectedCategoryId}>
                  Attribute Ekle
                </button>
              </form>
            </article>
          </section>

          <section className="card">
            <h2>Durum</h2>
            <p className="meta">{message}</p>
            {selectedCategory && (
              <>
                <h3>{selectedCategory.name} - Attributes</h3>
                <ul className="list">
                  {selectedCategory.attributes.map((attribute) => (
                    <li key={attribute.key}>
                      <strong>{attribute.key}</strong>
                      <span>
                        {attribute.type} | req:{String(attribute.required)} | filter:
                        {String(attribute.filterable)}
                      </span>
                    </li>
                  ))}
                </ul>
              </>
            )}
          </section>
        </>
      )}

      {viewMode === 'BROKER' && (
        <>
          <section className="grid two">
            <article className="card">
              <h2>1) Demo Broker Setup</h2>
              <form className="stack" onSubmit={handleBrokerLogin}>
                <label>
                  Broker Email
                  <input
                    value={brokerEmail}
                    onChange={(event) => setBrokerEmail(event.target.value)}
                    placeholder="ornek: broker.123@tradebridge.local"
                    required
                  />
                </label>
                <label>
                  Broker Sifre
                  <input
                    type="password"
                    value={brokerPassword}
                    onChange={(event) => setBrokerPassword(event.target.value)}
                    required
                  />
                </label>
                <button type="submit" disabled={loading}>
                  Broker Login
                </button>
              </form>
              <button type="button" onClick={setupDemoBroker} disabled={loading}>
                Demo Broker Olustur + Onayla
              </button>
              <p className="meta">
                Bu adim /auth/register-company ve /admin/companies/&#123;companyId&#125;/approve kullanir.
              </p>
            </article>

            <article className="card">
              <h2>2) RFQ Mudahale</h2>
              <form className="stack" onSubmit={handleBrokerIntervention}>
                <label>
                  RFQ ID
                  <input
                    value={rfqId}
                    onChange={(event) => setRfqId(event.target.value)}
                    placeholder="rfq id"
                    required
                  />
                </label>
                <label>
                  Note
                  <input
                    value={brokerNote}
                    onChange={(event) => setBrokerNote(event.target.value)}
                    required
                  />
                </label>
                <button type="submit" disabled={loading || !brokerToken}>
                  Mudahale Gonder
                </button>
              </form>
            </article>
          </section>

          <section className="card">
            <h2>Durum</h2>
            <p className="meta">{message}</p>
            {brokerResult && (
              <ul className="list">
                <li>
                  <strong>RFQ</strong>
                  <span>{brokerResult.rfqId}</span>
                </li>
                <li>
                  <strong>Broker User</strong>
                  <span>{brokerResult.brokerUserId}</span>
                </li>
                <li>
                  <strong>Note</strong>
                  <span>{brokerResult.note}</span>
                </li>
              </ul>
            )}
          </section>
        </>
      )}

      {viewMode === 'SELLER' && (
        <>
          <section className="grid two">
            <article className="card">
              <h2>1) Demo Seller Setup</h2>
              <form className="stack" onSubmit={handleSellerLogin}>
                <label>
                  Seller Email
                  <input
                    value={sellerEmail}
                    onChange={(event) => setSellerEmail(event.target.value)}
                    placeholder="ornek: seller.123@tradebridge.local"
                    required
                  />
                </label>
                <label>
                  Seller Sifre
                  <input
                    type="password"
                    value={sellerPassword}
                    onChange={(event) => setSellerPassword(event.target.value)}
                    required
                  />
                </label>
                <button type="submit" disabled={loading}>
                  Seller Login
                </button>
              </form>
              <button type="button" onClick={setupDemoSeller} disabled={loading}>
                Demo Seller Olustur + Onayla
              </button>
            </article>

            <article className="card">
              <h2>2) Draft Upload</h2>
              <form className="stack" onSubmit={handleSellerUpload}>
                <label>
                  Secili Kategori
                  <select
                    value={selectedCategoryId}
                    onChange={(event) => setSelectedCategoryId(event.target.value)}
                  >
                    <option value="">Kategori sec</option>
                    {categories.map((category) => (
                      <option key={category.id} value={category.id}>
                        {category.name}
                      </option>
                    ))}
                  </select>
                </label>
                <label>
                  Dosya (pdf/image/txt)
                  <input type="file" onChange={handleSellerFile} />
                </label>
                <button type="submit" disabled={loading || !sellerToken}>
                  Draft Upload
                </button>
              </form>
              <div className="stack">
                <button type="button" onClick={refreshSellerDraft} disabled={loading || !sellerDraft}>
                  Draft Yenile
                </button>
                <button type="button" onClick={confirmSellerDraft} disabled={loading || !sellerDraft}>
                  Draft Confirm
                </button>
              </div>
            </article>
          </section>

          <section className="card">
            <h2>Durum</h2>
            <p className="meta">{message}</p>
            {sellerDraft && (
              <ul className="list">
                <li>
                  <strong>Draft ID</strong>
                  <span>{sellerDraft.draftId}</span>
                </li>
                <li>
                  <strong>Status</strong>
                  <span>{sellerDraft.status}</span>
                </li>
                <li>
                  <strong>Parse Job</strong>
                  <span>{sellerDraft.parseJobId ?? '-'}</span>
                </li>
                <li>
                  <strong>Parsed</strong>
                  <span>{JSON.stringify(sellerDraft.parsedFields)}</span>
                </li>
                <li>
                  <strong>Confidence</strong>
                  <span>{JSON.stringify(sellerDraft.confidence)}</span>
                </li>
              </ul>
            )}
            {sellerProduct && (
              <ul className="list">
                <li>
                  <strong>Product ID</strong>
                  <span>{sellerProduct.productId}</span>
                </li>
                <li>
                  <strong>Attributes</strong>
                  <span>{JSON.stringify(sellerProduct.attributes)}</span>
                </li>
              </ul>
            )}
          </section>
        </>
      )}

      <header>
        <p className="subtitle">
          Varsayilan admin: <code>admin@tradebridge.local / admin123</code>
        </p>
      </header>
    </main>
  )
}

export default App
