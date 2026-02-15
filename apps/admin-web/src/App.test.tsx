import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import App from './App'

describe('Admin App', () => {
  it('renders management and broker title', () => {
    render(<App />)
    expect(screen.getByRole('heading', { name: 'Yonetim ve RFQ Mudahale' })).toBeInTheDocument()
  })
})
