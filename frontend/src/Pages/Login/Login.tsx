import {useState} from 'react'
import {Link} from 'react-router-dom'
import {Input} from '../../Components/Input/Input.tsx'
import {Button} from '../../Components/Button/Button.tsx'

type LoginProps = {
  onLogin: (username: string, password: string) => void | Promise<void>;
}

export const Login = ({ onLogin }: LoginProps) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [emailError, setEmailError] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [loginError, setLoginError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const validate = () => {
    let valid = true;

    if (!username.trim()) {
      setEmailError('Ange din mailadress.');
      valid = false;
    } else if (!username.includes('@')) {
      setEmailError('Ange en giltig mailadress (måste innehålla @).');
      valid = false;
    } else {
      setEmailError('');
    }

    if (!password) {
      setPasswordError('Ange ditt lösenord.');
      valid = false;
    } else if (password.length < 6) {
      setPasswordError('Lösenordet måste vara minst 6 tecken.');
      valid = false;
    } else {
      setPasswordError('');
    }

    return valid;
  };

  const handleSubmit = async () => {
    if (!validate()) {
      return;
    }

    setLoginError('');
    setIsSubmitting(true);
    try {
      await onLogin(username, password);
    } catch {
      setLoginError('Felaktig mailadress eller lösenord.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="flex h-screen items-center justify-center bg-gray-100">
      <div className="w-full max-w-md rounded-lg bg-white p-8 shadow-md">
        <h2 className="mb-6 text-center text-2xl font-bold text-gray-800">Logga in</h2>
        <p className="mb-6 text-left text-gray-600 text-sm">
          Välkommen tillbaka! Logga in med dina uppgifter.
        </p>
        {loginError && (
          <div role="alert" className="mb-4 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-600">
            {loginError}
          </div>
        )}
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleSubmit();
          }}
        >
          <Input
            label="Mailadress"
            value={username}
            onChange={setUsername}
            placeholder="Ange mailadress"
            className="mb-4 w-full text-left"
            error={emailError}
          />
          <Input
            label="Lösenord"
            value={password}
            onChange={setPassword}
            placeholder="Ange lösenord"
            type={showPassword ? 'text' : 'password'}
            className="mb-6 w-full text-left"
            error={passwordError}
            rightIcon={
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                aria-label={showPassword ? 'Dölj lösenord' : 'Visa lösenord'}
                aria-pressed={showPassword}
                className="text-neutral-400 hover:text-neutral-600 focus:outline-none"
              >
                <i className={`fa-solid ${showPassword ? 'fa-eye-slash' : 'fa-eye'}`} />
              </button>
            }
          />
          <div className="mb-4 flex justify-end text-xs font-bold">
           <Link to="/glomt-losenord" className="text-[#00c281] hover:text-[#00a86b]"> Glömt lösenord? </Link>
          </div>
          
          <Button
            label={isSubmitting ? 'Loggar in…' : 'Logga in'}
            onClick={handleSubmit}
            disabled={isSubmitting}
            className="w-full mb-4"
          />
            <div className="flex items-center justify-center mb-4">
            eller 
          </div>
          <Button
            label="Fortsätt med Google"
            variant="secondary"
            onClick={() => onLogin(username, password)}
            className="w-full"
            icon={
              <svg width="18" height="18" viewBox="0 0 48 48" aria-hidden="true">
                <path fill="#4285F4" d="M45.12 24.5c0-1.56-.14-3.06-.4-4.5H24v8.51h11.84c-.51 2.75-2.06 5.08-4.39 6.64v5.52h7.11c4.16-3.83 6.56-9.47 6.56-16.17z" />
                <path fill="#34A853" d="M24 46c5.94 0 10.92-1.97 14.56-5.33l-7.11-5.52c-1.97 1.32-4.49 2.1-7.45 2.1-5.73 0-10.58-3.87-12.31-9.07H4.34v5.7C7.96 41.07 15.4 46 24 46z" />
                <path fill="#FBBC05" d="M11.69 28.18C11.25 26.86 11 25.45 11 24s.25-2.86.69-4.18v-5.7H4.34C2.85 17.09 2 20.45 2 24s.85 6.91 2.34 9.88l7.35-5.7z" />
                <path fill="#EA4335" d="M24 10.75c3.23 0 6.13 1.11 8.41 3.29l6.31-6.31C34.91 4.18 29.93 2 24 2 15.4 2 7.96 6.93 4.34 14.12l7.35 5.7c1.73-5.2 6.58-9.07 12.31-9.07z" />
              </svg>
            }
          />
        </form>
      </div>
    </div>
  );
};