import { StatusBar } from 'expo-status-bar'
import { SafeAreaView, StyleSheet, Text, View } from 'react-native'

const roles = [
  {
    id: 'BUYER',
    title: 'Alici',
    note: 'Urunleri listele, filtrele, AI ile soru sor ve RFQ olustur.',
  },
  {
    id: 'SELLER',
    title: 'Satici',
    note: 'Dokuman yukle, parse wizard ile onayla, urun yayinla.',
  },
]

export default function App() {
  return (
    <SafeAreaView style={styles.safe}>
      <StatusBar style="dark" />
      <View style={styles.container}>
        <Text style={styles.eyebrow}>trade-bridge mobile</Text>
        <Text style={styles.title}>Alici/Satici Uygulamasi</Text>
        <Text style={styles.subtitle}>MVP iskeleti aktif</Text>

        {roles.map((role) => (
          <View key={role.id} style={styles.card}>
            <Text style={styles.cardTitle}>{role.title}</Text>
            <Text style={styles.cardBody}>{role.note}</Text>
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
