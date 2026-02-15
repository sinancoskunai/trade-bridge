import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import App from './App'

describe('Admin App', () => {
  it('renders category management title', () => {
    render(<App />)
    expect(screen.getByRole('heading', { name: 'Kategori Yonetimi' })).toBeInTheDocument()
  })
})
