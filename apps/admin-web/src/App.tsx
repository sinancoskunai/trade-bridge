import './App.css'

import { useCallback, useMemo, useState } from 'react'
import type { FormEvent } from 'react'

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

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

const typeOptions = ['STRING', 'NUMBER', 'BOOLEAN', 'ENUM', 'DATE']

function parseEnumValues(raw: string): string[] | null {
  const values = raw
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
  return values.length > 0 ? values : null
}

function App() {
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

  const selectedCategory = useMemo(
    () => categories.find((item) => item.id === selectedCategoryId) ?? null,
    [categories, selectedCategoryId],
  )

  const fetchCategories = useCallback(
    async (accessToken: string) => {
      const response = await fetch(`${API_BASE_URL}/categories`, {
        headers: { Authorization: `Bearer ${accessToken}` },
      })

      if (!response.ok) {
        throw new Error(`Kategori listesi alinamadi: ${response.status}`)
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

  const handleLogin = useCallback(
    async (event: FormEvent<HTMLFormElement>) => {
      event.preventDefault()
      setLoading(true)
      setMessage('Login deneniyor...')
      try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email, password }),
        })
        if (!response.ok) {
          throw new Error(`Login basarisiz: ${response.status}`)
        }
        const data = (await response.json()) as LoginResponse
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
    [email, password, fetchCategories],
  )

  const handleCreateCategory = useCallback(
    async (event: FormEvent<HTMLFormElement>) => {
      event.preventDefault()
      if (!token) {
        setMessage('Once login olmalisin.')
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
          throw new Error(`Kategori olusturulamadi: ${response.status}`)
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
          throw new Error(`Attribute eklenemedi: ${response.status}`)
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

  return (
    <main className="page">
      <header>
        <p className="eyebrow">trade-bridge admin</p>
        <h1>Kategori Yonetimi</h1>
        <p className="subtitle">
          API: <code>{API_BASE_URL}</code>
        </p>
      </header>

      <section className="grid two">
        <article className="card">
          <h2>1) Login</h2>
          <form className="stack" onSubmit={handleLogin}>
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

      <header>
        <p className="subtitle">
          Login test: <code>admin@tradebridge.local / admin123</code>
        </p>
      </header>
    </main>
  )
}

export default App
