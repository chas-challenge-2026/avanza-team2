import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Input } from '../../Components/Input/Input.tsx';
import { Button } from '../../Components/Button/Button.tsx';

export const ForgotPassword = () => {
  const [email, setEmail] = useState('');
  const [emailError, setEmailError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  const validate = () => {
    if (!email.trim()) {
      setEmailError('Ange din mailadress.');
      return false;
    }
    if (!email.includes('@')) {
      setEmailError('Ange en giltig mailadress (måste innehålla @).');
      return false;
    }
    setEmailError('');
    return true;
  };

  const handleSubmit = async () => {
    if (!validate()) {
      return;
    }
    setIsSubmitting(true);
    // No backend yet — this is where the reset-email request will go.
    await new Promise((resolve) => setTimeout(resolve, 400));
    setIsSubmitting(false);
    setSubmitted(true);
  };

  return (
    <div className="flex h-screen items-center justify-center bg-neutral-100">
      <div className="w-full max-w-md rounded-lg bg-white p-8 shadow-md">
        {submitted ? (
          <div className="text-center">
            <div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-[#e2f7ec]">
              <i className="fa-solid fa-envelope-circle-check text-xl text-[#00c281]" />
            </div>
            <h2 className="mb-2 text-2xl font-bold text-neutral-800">Kolla din inkorg</h2>
            <p className="mb-6 text-sm text-neutral-600">
              Om <span className="font-medium text-neutral-800">{email}</span> finns hos oss har vi
              skickat en länk dit för att återställa ditt lösenord.
            </p>
            <Link to="/Loggain" className="text-sm font-semibold text-[#00c281] hover:text-[#00a86b]">
              Tillbaka till inloggning
            </Link>
          </div>
        ) : (
          <>
            <h2 className="mb-2 text-center text-2xl font-bold text-neutral-800">Glömt lösenord?</h2>
            <p className="mb-6 text-left text-sm text-neutral-600">
              Ange din mailadress så skickar vi en länk för att återställa ditt lösenord.
            </p>
            <form
              onSubmit={(e) => {
                e.preventDefault();
                handleSubmit();
              }}
            >
              <Input
                label="Mailadress"
                value={email}
                onChange={setEmail}
                placeholder="Ange mailadress"
                className="mb-6 w-full text-left"
                error={emailError}
              />
              <Button
                label={isSubmitting ? 'Skickar…' : 'Skicka återställningslänk'}
                onClick={handleSubmit}
                disabled={isSubmitting}
                className="mb-4 w-full"
              />
              <div className="text-center">
                <Link to="/Loggain" className="text-sm text-neutral-500 hover:text-neutral-700">
                  Tillbaka till inloggning
                </Link>
              </div>
            </form>
          </>
        )}
      </div>
    </div>
  );
};
