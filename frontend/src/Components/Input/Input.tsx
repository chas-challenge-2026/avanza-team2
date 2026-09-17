type InputProps = {
    label: string;
    value: string;
    onChange : (value: string) => void;
    placeholder?: string;
    type?: 'text' | 'number' | 'password';
    className?: string;
    rightIcon?: React.ReactNode;
    error?: string;
    id?: string;
}
export const Input = (props: InputProps) => {
  const inputId = props.id ?? props.label.replace(/\s+/g, '-').toLowerCase();
  const errorId = `${inputId}-error`;

  return (
    <div className={`flex flex-col gap-1 ${props.className ?? ''}`}>
      <label htmlFor={inputId} className="text-sm font-medium text-neutral-900">{props.label}</label>
      <div className="relative">
        <input
          id={inputId}
          className={`w-full rounded-md border px-3 py-2 text-sm text-neutral-900 placeholder:text-neutral-400 focus:ring-blue-500 disabled:cursor-not-allowed disabled:opacity-50 ${
            props.error ? 'border-red-400 focus:border-red-400' : 'border-neutral-200 focus:border-blue-500'
          }`}
          type={props.type ?? 'text'}
          value={props.value}
          onChange={(e) => props.onChange(e.target.value)}
          placeholder={props.placeholder}
          aria-invalid={props.error ? true : undefined}
          aria-describedby={props.error ? errorId : undefined}
        />
        {props.rightIcon && <div className="absolute right-3 top-1/2 -translate-y-1/2">{props.rightIcon}</div>}
      </div>
      {props.error && (
        <p id={errorId} role="alert" className="text-xs text-red-500">
          {props.error}
        </p>
      )}
    </div>
  )
}
