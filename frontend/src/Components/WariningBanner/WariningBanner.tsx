interface WariningBannerProps {
  message: string;
  title?: string;
  className?: string;
}

export const WariningBanner = ({
  message,
  title = 'Varning!',
  className,
}: WariningBannerProps) => {
  return (
    <div
      className={`flex items-start gap-3 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3${
        className ? ` ${className}` : ''
      }`}
    >
      <i className="fa-solid fa-triangle-exclamation mt-0.5 text-amber-500" />
      <p className="text-sm text-amber-900">
        <span className="font-bold">{title}</span> {message}
      </p>
    </div>
  );
};
