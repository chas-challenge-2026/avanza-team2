import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Input } from '../../Components/Input/Input.tsx';
import { Button } from '../../Components/Button/Button.tsx';

export const ResetPassword = () => {
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [passwordError, setPasswordError] = useState('');
  const [confirmError, setConfirmError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [done, setDone] = useState(false);

  const validate = () => {
    let valid = true;

    if (!password) {
      setPasswordError('Ange ett nytt lösenord.');
      valid = false;
    } else if (password.length < 6) {
      setPasswordError('Lösenordet måste vara minst 6 tecken.');
      valid = false;
    } else {
      setPasswordError('');
    }

    if (!confirmPassword) {
      setConfirmError('Bekräfta ditt nya lösenord.');
      valid = false;
    } else if (confirmPassword !== password) {
      setConfirmError('Lösenorden matchar inte.');
      valid = false;
    } else {
      setConfirmError('');
    }

    return valid;
  };

  const handleSubmit = async () => {
    if (!validate()) {
      return;
    }
    setIsSubmitting(true);
    // No backend yet — this is where the token + new password gets sent.
    await new Promise((resolve) => setTimeout(resolve, 400));
    setIsSubmitting(false);
    setDone(true);
  };

  const toggleIcon = (
    <button
      type="button"
      onClick={() => setShowPassword(!showPassword)}
      aria-label={showPassword ? 'Dölj lösenord' : 'Visa lösenord'}
      aria-pressed={showPassword}
      className="text-neutral-400 hover:text-neutral-600 focus:outline-none"
    >
      <i className={`fa-solid ${showPassword ? 'fa-eye-slash' : 'fa-eye'}`} />
    </button>
  );

  return (
    <div className="flex h-screen items-center justify-center bg-neutral-100">
      <div className="w-full max-w-md rounded-lg bg-white p-8 shadow-md">
        {done ? (
          <div className="text-center">
            <div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-[#e2f7ec]">
              <i className="fa-solid fa-circle-check text-xl text-[#00c281]" />
            </div>
            <h2 className="mb-2 text-2xl font-bold text-neutral-800">Lösenordet är uppdaterat</h2>
            <p className="mb-6 text-sm text-neutral-600">
              Du kan nu logga in med ditt nya lösenord.
            </p>
            <Link
              to="/Loggain"
              className="inline-block rounded-md bg-[#00c281] px-4 py-2 text-sm font-medium text-white hover:bg-[#00a86b]"
            >
              Till inloggning
            </Link>
          </div>
        ) : (
          <>
            <h2 className="mb-2 text-center text-2xl font-bold text-neutral-800">Skapa nytt lösenord</h2>
            <p className="mb-6 text-left text-sm text-neutral-600">
              Ange ditt nya lösenord nedan.
            </p>
            <form
              onSubmit={(e) => {
                e.preventDefault();
                handleSubmit();
              }}
            >
              <Input
                label="Nytt lösenord"
                value={password}
                onChange={setPassword}
                placeholder="Ange nytt lösenord"
                type={showPassword ? 'text' : 'password'}
                className="mb-4 w-full text-left"
                error={passwordError}
                rightIcon={toggleIcon}
              />
              <Input
                label="Bekräfta lösenord"
                value={confirmPassword}
                onChange={setConfirmPassword}
                placeholder="Upprepa nytt lösenord"
                type={showPassword ? 'text' : 'password'}
                className="mb-6 w-full text-left"
                error={confirmError}
              />
              <Button
                label={isSubmitting ? 'Sparar…' : 'Spara nytt lösenord'}
                onClick={handleSubmit}
                disabled={isSubmitting}
                className="w-full"
              />
            </form>
          </>
        )}
      </div>
    </div>
  );
};
