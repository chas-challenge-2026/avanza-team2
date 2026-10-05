type ButtonProps = {
    label: string;
    onClick: ()=> void;
    variant?: 'primary' | 'secondary';
    icon?: React.ReactNode;
    disabled?: boolean;
    pill?: boolean;
    className?: string;
}

export const Button = (props : ButtonProps) => {
  const variantClasses = props.variant === 'secondary' ? 'bg-white text-neutral-700 border border-neutral-200 hover:bg-neutral-50' : 'bg-[#00c281] text-white hover:bg-[#00a86b]';
  const shapeClass = props.pill ? 'rounded-full' : 'rounded-md';

  return (
    <button
      type="button"
      onClick={props.onClick}
      disabled={props.disabled}
      className={`flex items-center justify-center gap-2 ${shapeClass} px-4 py-2 text-sm font-medium focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 ${variantClasses} ${props.className ?? ''}`}
    >
      {props.icon}
      {props.label}
    </button>
  )
}
