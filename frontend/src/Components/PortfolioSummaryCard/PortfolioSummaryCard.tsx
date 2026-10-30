interface PortfolioSummaryCardProps {
  totalValue: string;
  investedValue: string;
  currencyLabel: string;
  className?: string;
}

export const PortfolioSummaryCard = ({
  totalValue,
  investedValue,
  currencyLabel,
  className,
}: PortfolioSummaryCardProps) => {
  return (
    <div
      className={`rounded-2xl border border-[#d3f3e6] bg-[#eafaf3] p-4${
        className ? ` ${className}` : ''
      }`}
    >
      <div className="text-left">
        <span className="text-xs font-bold tracking-wide text-neutral-500">
          TOTALT VÄRDE
        </span>
        <div className="mt-1 text-3xl font-bold text-black">{totalValue}</div>
      </div>

      <div className="my-3 border-t border-[#c5ecda]" />

      <div className="flex items-center justify-between text-left">
        <div>
          <span className="text-xs font-bold text-neutral-600">Totalt investerat</span>
          <div className="mt-1 text-xs font-bold text-black">{investedValue}</div>
        </div>

        <div>
          <span className="text-xs font-bold text-neutral-600">Valuta</span>
          <div className="mt-1 text-xs font-bold text-black">{currencyLabel}</div>
        </div>
      </div>
    </div>
  );
};
