import { useCallback, useState } from 'react'
import { StatusBar } from 'expo-status-bar'
import { Pressable, SafeAreaView, StyleSheet, Text, View } from 'react-native'

type Category = {
  id: string
  name: string
}

const API_BASE_URL =
  process.env.EXPO_PUBLIC_API_BASE_URL ?? 'http://192.168.1.20:8080'

export default function App() {
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('Backend baglantisi hazir degil')
  const [categories, setCategories] = useState<Category[]>([])

  const loadCategories = useCallback(async () => {
    setLoading(true)
    setMessage('Backend baglantisi kuruluyor...')
    try {
      const loginResponse = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: 'admin@tradebridge.local',
          password: 'admin123',
        }),
      })

      if (!loginResponse.ok) {
        throw new Error(`Login failed: ${loginResponse.status}`)
      }

      const loginData = await loginResponse.json()
      const accessToken = loginData.accessToken as string

      const categoriesResponse = await fetch(`${API_BASE_URL}/categories`, {
        headers: { Authorization: `Bearer ${accessToken}` },
      })

      if (!categoriesResponse.ok) {
        throw new Error(`Category fetch failed: ${categoriesResponse.status}`)
      }

      const data = (await categoriesResponse.json()) as Category[]
      setCategories(data)
      setMessage(`Baglanti tamam. ${data.length} kategori bulundu.`)
    } catch (error) {
      const reason = error instanceof Error ? error.message : 'unknown'
      setMessage(`Baglanti hatasi: ${reason}`)
    } finally {
      setLoading(false)
    }
  }, [])

  return (
    <SafeAreaView style={styles.safe}>
      <StatusBar style="dark" />
      <View style={styles.container}>
        <Text style={styles.eyebrow}>trade-bridge mobile</Text>
        <Text style={styles.title}>Backend Baglanti Testi</Text>
        <Text style={styles.subtitle}>API: {API_BASE_URL}</Text>

        <Pressable style={styles.button} onPress={loadCategories}>
          <Text style={styles.buttonText}>
            {loading ? 'Yukleniyor...' : 'Backendden Kategori Cek'}
          </Text>
        </Pressable>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Durum</Text>
          <Text style={styles.cardBody}>{message}</Text>
        </View>

        {categories.map((category) => (
          <View key={category.id} style={styles.card}>
            <Text style={styles.cardTitle}>{category.name}</Text>
            <Text style={styles.cardBody}>ID: {category.id}</Text>
          </View>
        ))}
      </View>
    </SafeAreaView>
  )
}

const styles = StyleSheet.create({
  safe: {
    flex: 1,
    backgroundColor: '#f3f6fb',
  },
  container: {
    flex: 1,
    padding: 20,
    gap: 10,
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
  card: {
    backgroundColor: '#fff',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#dbe4f2',
    padding: 16,
  },
  cardTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#111f33',
  },
  cardBody: {
    marginTop: 8,
    fontSize: 14,
    lineHeight: 20,
    color: '#4f617d',
  },
})
