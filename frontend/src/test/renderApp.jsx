import { render } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import App from '../App.jsx'
import { AuthProvider } from '../auth/AuthProvider.jsx'

export function renderApp(path = '/') {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <AuthProvider>
        <App />
      </AuthProvider>
    </MemoryRouter>,
  )
}
