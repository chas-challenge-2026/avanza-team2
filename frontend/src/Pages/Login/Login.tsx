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
            className="w-full"
          />
        </form>
      </div>
    </div>
  );
};