import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { Login } from './Login'

describe('Login page', () => {
  it('shows validation errors when fields are empty', async () => {
    const user = userEvent.setup()
    const onLogin = jest.fn()

    render(
      <MemoryRouter>
        <Login onLogin={onLogin} />
      </MemoryRouter>
    )

    await user.click(screen.getByRole('button', { name: /logga in/i }))

    expect(screen.getByText(/ange din mailadress/i)).toBeInTheDocument()
    expect(screen.getByText(/ange ditt lösenord/i)).toBeInTheDocument()
    expect(onLogin).not.toHaveBeenCalled()
  })

  it('calls onLogin with the entered email and password when valid', async () => {
    const user = userEvent.setup()
    const onLogin = jest.fn().mockResolvedValue(undefined)

    render(
      <MemoryRouter>
        <Login onLogin={onLogin} />
      </MemoryRouter>
    )

    await user.type(screen.getByLabelText(/mailadress/i), 'test@example.com')
    await user.type(screen.getByLabelText(/^lösenord$/i), 'secret123')

    await user.click(screen.getByRole('button', { name: /logga in/i }))

    expect(onLogin).toHaveBeenCalledWith('test@example.com', 'secret123')
  })

  it('shows an error alert when login fails', async () => {
    const user = userEvent.setup()
    const onLogin = jest.fn().mockRejectedValue(new Error('bad credentials'))

    render(
      <MemoryRouter>
        <Login onLogin={onLogin} />
      </MemoryRouter>
    )

    await user.type(screen.getByLabelText(/mailadress/i), 'test@example.com')
    await user.type(screen.getByLabelText(/^lösenord$/i), 'secret123')

    await user.click(screen.getByRole('button', { name: /logga in/i }))

    expect(
      await screen.findByText(/felaktig mailadress eller lösenord/i)
    ).toBeInTheDocument()
  })

  it('toggles password visibility', async () => {
    const user = userEvent.setup()
    const onLogin = jest.fn()

    render(
      <MemoryRouter>
        <Login onLogin={onLogin} />
      </MemoryRouter>
    )

    const passwordInput = screen.getByLabelText(/^lösenord$/i)

    expect(passwordInput).toHaveAttribute('type', 'password')

    await user.click(screen.getByRole('button', { name: /visa lösenord/i }))

    expect(passwordInput).toHaveAttribute('type', 'text')
  })
})