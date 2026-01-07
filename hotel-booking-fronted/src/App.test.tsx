import { describe, it, expect } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import App from './App'

describe('App', () => {
  it('renders the main heading', () => {
    render(<App />)
    expect(screen.getByText('Vite + React')).toBeInTheDocument()
  })

  it('renders Vite and React logos', () => {
    render(<App />)
    expect(screen.getByAltText('Vite logo')).toBeInTheDocument()
    expect(screen.getByAltText('React logo')).toBeInTheDocument()
  })

  it('renders counter button with initial value of 0', () => {
    render(<App />)
    expect(screen.getByRole('button', { name: /count is 0/i })).toBeInTheDocument()
  })

  it('increments counter when button is clicked', () => {
    render(<App />)
    const button = screen.getByRole('button', { name: /count is 0/i })
    
    fireEvent.click(button)
    expect(screen.getByRole('button', { name: /count is 1/i })).toBeInTheDocument()
    
    fireEvent.click(button)
    expect(screen.getByRole('button', { name: /count is 2/i })).toBeInTheDocument()
  })

  it('renders the documentation hint', () => {
    render(<App />)
    expect(screen.getByText(/Click on the Vite and React logos to learn more/i)).toBeInTheDocument()
  })
})
