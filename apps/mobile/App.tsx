import { useCallback, useMemo, useState } from 'react'
import { StatusBar } from 'expo-status-bar'
import {
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native'

type Role = 'BUYER' | 'SELLER' | 'BROKER' | 'ADMIN'

type Category = {
  id: string
  name: string
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

type SearchQaResponse = {
  interpretedFilters: Record<string, string>
  followUpQuestions: string[]
  explanation: string
}

type RfqResponse = {
  rfqId: string
  buyerUserId: string
  categoryId: string
  requirementText: string
  status: string
}

const API_BASE_URL = process.env.EXPO_PUBLIC_API_BASE_URL ?? 'http://192.168.1.20:8080'

export default function App() {
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('Hazir')

  const [buyerEmail, setBuyerEmail] = useState('')
  const [buyerPassword, setBuyerPassword] = useState('buyer123')
  const [buyerToken, setBuyerToken] = useState<string | null>(null)

  const [categories, setCategories] = useState<Category[]>([])
  const [selectedCategoryId, setSelectedCategoryId] = useState('')
  const [queryText, setQueryText] = useState('kuruyemis 10 kg tr mensei')

  const [searchResult, setSearchResult] = useState<SearchQaResponse | null>(null)
  const [rfqResult, setRfqResult] = useState<RfqResponse | null>(null)

  const selectedCategory = useMemo(
    () => categories.find((item) => item.id === selectedCategoryId) ?? null,
    [categories, selectedCategoryId],
  )

  const login = useCallback(async (email: string, password: string): Promise<LoginResponse> => {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    })

    if (!response.ok) {
      throw new Error(`Login failed: ${response.status}`)
    }

    return (await response.json()) as LoginResponse
  }, [])

  const loadCategories = useCallback(async (accessToken: string) => {
    const response = await fetch(`${API_BASE_URL}/categories`, {
      headers: { Authorization: `Bearer ${accessToken}` },
    })

    if (!response.ok) {
      throw new Error(`Category fetch failed: ${response.status}`)
    }

    const data = (await response.json()) as Category[]
    setCategories(data)
    if (data.length > 0) {
      setSelectedCategoryId((current) => current || data[0].id)
    }
    return data
  }, [])

  const setupDemoBuyer = useCallback(async () => {
    setLoading(true)
    setMessage('Demo buyer olusturuluyor...')
    try {
      const nonce = Date.now()
      const demoEmail = `buyer.${nonce}@tradebridge.local`
      const demoCompany = `Buyer Co ${nonce}`

      const registerResponse = await fetch(`${API_BASE_URL}/auth/register-company`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          companyName: demoCompany,
          email: demoEmail,
          password: buyerPassword,
          role: 'BUYER',
        }),
      })

      if (!registerResponse.ok) {
        throw new Error(`Buyer register failed: ${registerResponse.status}`)
      }

      const approval = (await registerResponse.json()) as CompanyApprovalResponse
      const admin = await login('admin@tradebridge.local', 'admin123')

      const approveResponse = await fetch(
        `${API_BASE_URL}/admin/companies/${approval.companyId}/approve`,
        {
          method: 'POST',
          headers: { Authorization: `Bearer ${admin.accessToken}` },
        },
      )
      if (!approveResponse.ok) {
        throw new Error(`Company approve failed: ${approveResponse.status}`)
      }

      setBuyerEmail(demoEmail)
      setMessage(`Demo buyer hazir: ${demoEmail}`)
    } catch (error) {
      const reason = error instanceof Error ? error.message : 'unknown'
      setMessage(`Hata: ${reason}`)
    } finally {
      setLoading(false)
    }
  }, [buyerPassword, login])

  const handleBuyerLogin = useCallback(async () => {
    if (!buyerEmail.trim()) {
      setMessage('Buyer email gerekli')
      return
    }

    setLoading(true)
    setMessage('Buyer login deneniyor...')
    try {
      const data = await login(buyerEmail, buyerPassword)
      if (data.role !== 'BUYER') {
        throw new Error(`Unexpected role: ${data.role}`)
      }

      setBuyerToken(data.accessToken)
      const listed = await loadCategories(data.accessToken)
      setMessage(`Buyer login ok. ${listed.length} kategori yuklendi.`)
    } catch (error) {
      const reason = error instanceof Error ? error.message : 'unknown'
      setMessage(`Hata: ${reason}`)
    } finally {
      setLoading(false)
    }
  }, [buyerEmail, buyerPassword, login, loadCategories])

  const handleQaSearch = useCallback(async () => {
    if (!buyerToken) {
      setMessage('Once buyer login ol')
      return
    }

    setLoading(true)
    setMessage('AI QA filtre calisiyor...')
    try {
      const response = await fetch(`${API_BASE_URL}/buyer/search/qa`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${buyerToken}`,
        },
        body: JSON.stringify({
          queryText,
          categoryId: selectedCategoryId || null,
          contextFilters: {},
        }),
      })

      if (!response.ok) {
        throw new Error(`QA search failed: ${response.status}`)
      }

      const data = (await response.json()) as SearchQaResponse
      setSearchResult(data)
      setMessage('AI QA filtre sonucu alindi.')
    } catch (error) {
      const reason = error instanceof Error ? error.message : 'unknown'
      setMessage(`Hata: ${reason}`)
    } finally {
      setLoading(false)
    }
  }, [buyerToken, queryText, selectedCategoryId])

  const handleCreateRfq = useCallback(async () => {
    if (!buyerToken) {
      setMessage('Once buyer login ol')
      return
    }
    if (!selectedCategoryId) {
      setMessage('Kategori secmelisin')
      return
    }

    setLoading(true)
    setMessage('RFQ olusturuluyor...')
    try {
      const response = await fetch(`${API_BASE_URL}/buyer/rfqs`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${buyerToken}`,
        },
        body: JSON.stringify({
          categoryId: selectedCategoryId,
          requirementText: queryText,
        }),
      })

      if (!response.ok) {
        throw new Error(`RFQ create failed: ${response.status}`)
      }

      const data = (await response.json()) as RfqResponse
      setRfqResult(data)
      setMessage(`RFQ olustu: ${data.rfqId}`)
    } catch (error) {
      const reason = error instanceof Error ? error.message : 'unknown'
      setMessage(`Hata: ${reason}`)
    } finally {
      setLoading(false)
    }
  }, [buyerToken, queryText, selectedCategoryId])

  return (
    <SafeAreaView style={styles.safe}>
      <StatusBar style="dark" />
      <ScrollView contentContainerStyle={styles.container}>
        <Text style={styles.eyebrow}>trade-bridge mobile</Text>
        <Text style={styles.title}>Buyer Search + RFQ</Text>
        <Text style={styles.subtitle}>API: {API_BASE_URL}</Text>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>1) Demo Buyer Setup</Text>
          <TextInput
            style={styles.input}
            placeholder="buyer email"
            value={buyerEmail}
            onChangeText={setBuyerEmail}
            autoCapitalize="none"
          />
          <TextInput
            style={styles.input}
            placeholder="buyer password"
            value={buyerPassword}
            onChangeText={setBuyerPassword}
            secureTextEntry
          />
          <Pressable style={styles.button} onPress={setupDemoBuyer}>
            <Text style={styles.buttonText}>
              {loading ? 'Yukleniyor...' : 'Demo Buyer Olustur + Onayla'}
            </Text>
          </Pressable>
          <Pressable style={styles.buttonSecondary} onPress={handleBuyerLogin}>
            <Text style={styles.buttonSecondaryText}>Buyer Login</Text>
          </Pressable>
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>2) Kategori ve Soru</Text>
          <Text style={styles.cardBody}>Secili kategori: {selectedCategory?.name ?? '-'}</Text>
          <TextInput
            style={styles.input}
            placeholder="category id"
            value={selectedCategoryId}
            onChangeText={setSelectedCategoryId}
          />
          <TextInput
            style={[styles.input, styles.textArea]}
            placeholder="ihtiyac metni"
            value={queryText}
            onChangeText={setQueryText}
            multiline
          />
          <Pressable style={styles.button} onPress={handleQaSearch}>
            <Text style={styles.buttonText}>AI QA Filtre Calistir</Text>
          </Pressable>
          <Pressable style={styles.buttonSecondary} onPress={handleCreateRfq}>
            <Text style={styles.buttonSecondaryText}>RFQ Olustur</Text>
          </Pressable>
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Durum</Text>
          <Text style={styles.cardBody}>{message}</Text>
        </View>

        {searchResult && (
          <View style={styles.card}>
            <Text style={styles.cardTitle}>AI QA Sonucu</Text>
            <Text style={styles.cardBody}>{searchResult.explanation}</Text>
            <Text style={styles.cardBody}>
              Filtreler: {JSON.stringify(searchResult.interpretedFilters)}
            </Text>
            <Text style={styles.cardBody}>
              Follow-ups: {searchResult.followUpQuestions.join(' | ') || '-'}
            </Text>
          </View>
        )}

        {rfqResult && (
          <View style={styles.card}>
            <Text style={styles.cardTitle}>RFQ</Text>
            <Text style={styles.cardBody}>RFQ ID: {rfqResult.rfqId}</Text>
            <Text style={styles.cardBody}>Status: {rfqResult.status}</Text>
            <Text style={styles.cardBody}>Bu ID'yi broker panelinde mudahale icin kullan.</Text>
          </View>
        )}

        {categories.map((category) => (
          <View key={category.id} style={styles.card}>
            <Text style={styles.cardTitle}>{category.name}</Text>
            <Text style={styles.cardBody}>ID: {category.id}</Text>
          </View>
        ))}
      </ScrollView>
    </SafeAreaView>
  )
}

const styles = StyleSheet.create({
  safe: {
    flex: 1,
    backgroundColor: '#f3f6fb',
  },
  container: {
    padding: 20,
    gap: 10,
    paddingBottom: 30,
  },
  eyebrow: {
    marginTop: 8,
    fontSize: 12,
    color: '#4f617d',
    textTransform: 'uppercase',
    letterSpacing: 0.8,
  },
  title: {
    fontSize: 28,
    fontWeight: '700',
    color: '#111f33',
  },
  subtitle: {
    fontSize: 14,
    color: '#4f617d',
    marginBottom: 8,
  },
  button: {
    backgroundColor: '#0d6dff',
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingVertical: 12,
    marginBottom: 8,
  },
  buttonText: {
    color: '#fff',
    fontSize: 14,
    fontWeight: '700',
    textAlign: 'center',
  },
  buttonSecondary: {
    backgroundColor: '#ffffff',
    borderColor: '#0d6dff',
    borderWidth: 1,
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingVertical: 12,
    marginBottom: 8,
  },
  buttonSecondaryText: {
    color: '#0d6dff',
    fontSize: 14,
    fontWeight: '700',
    textAlign: 'center',
  },
  card: {
    backgroundColor: '#fff',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#dbe4f2',
    padding: 16,
    gap: 8,
  },
  cardTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#111f33',
  },
  cardBody: {
    marginTop: 2,
    fontSize: 14,
    lineHeight: 20,
    color: '#4f617d',
  },
  input: {
    backgroundColor: '#ffffff',
    borderColor: '#dbe4f2',
    borderWidth: 1,
    borderRadius: 10,
    paddingHorizontal: 12,
    paddingVertical: 10,
    color: '#111f33',
  },
  textArea: {
    minHeight: 80,
    textAlignVertical: 'top',
  },
})
