import { useState, useEffect, useMemo } from 'react';
import SearchInput from '../../Components/SearchInput/SearchInput';
import {
    DropdownBtn,
    type DropdownOption,
} from '../../Components/DropdownBtn/DropdownBtn';
import HoldingRow, {
    type HoldingItem,
} from '../../Components/HoldingRow/HoldingRow';
import './Holdings.css';

// API base URL from the frontend environment configuration.
const API_URL = import.meta.env.VITE_API_URL ?? '';

// Available account filter options.
const ACCOUNT_OPTIONS: DropdownOption[] = [
    { label: 'Alla konton', value: 'all' },
    { label: 'ISK', value: 'isk' },
    { label: 'KF', value: 'kf' },
    { label: 'Depå', value: 'depa' },
];

// Available currency filter options.
const CURRENCY_OPTIONS: DropdownOption[] = [
    { label: 'SEK', value: 'sek' },
    { label: 'EUR', value: 'eur' },
    { label: 'USD', value: 'usd' },
];

// Represents a single holding returned by the backend API.
interface ApiHolding {
    id: number;
    ticker: string;
    instrument_name: string;
    quantity: number | string;
    avg_buy_price: number | string | null;
    currency: string | null;
    account_type: string | null;
    account_name: string | null;
    currentPrice: number | null;
    marketValue: number | null;
    pnl: number | null;
}

// Represents the paginated response returned by the holdings endpoint.
interface HoldingsResponse {
    userName: string;
    holdings: ApiHolding[];
    accounts: {
        id: number;
        account_type: string;
        account_name: string;
    }[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}

// Extends the existing frontend model with additional backend properties.
interface HoldingViewItem extends HoldingItem {
    accountType: string;
    accountName: string;
    currency: string;
    pnl: number | null;
}

// Safely converts numeric values returned by the backend.
const toNumber = (
    value: number | string | null | undefined,
): number => {
    if (value === null || value === undefined) {
        return 0;
    }

    const parsed = Number(value);

    return Number.isFinite(parsed) ? parsed : 0;
};

// Normalizes strings to make searching and filtering case-insensitive.
const normalize = (value: string | null | undefined): string =>
    (value ?? '').trim().toLowerCase();

// Formats monetary values using Swedish number formatting.
const formatCurrency = (value: number): string =>
    `${value.toLocaleString('sv-SE', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 2,
    })} SEK`;

const Holdings = () => {
    // Stores the holdings retrieved from the backend.
    const [holdings, setHoldings] = useState<HoldingViewItem[]>([]);

    // Tracks the loading state and any API errors.
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // Stores the current search query and selected filters.
    const [searchQuery, setSearchQuery] = useState('');
    const [selectedAccount, setSelectedAccount] = useState('all');
    const [selectedCurrency, setSelectedCurrency] = useState('sek');

    // Fetches the authenticated user's holdings when the component mounts.
    useEffect(() => {
        const controller = new AbortController();

        const fetchHoldings = async () => {
            try {
                setLoading(true);
                setError(null);

                // Include cookies so the backend can identify the authenticated user.
                const response = await fetch(
                    `${API_URL}/api/holdings?page=0&size=100`,
                    {
                        method: 'GET',
                        credentials: 'include',
                        signal: controller.signal,
                    },
                );

                // Handle unsuccessful HTTP responses.
                if (!response.ok) {
                    if (response.status === 401) {
                        throw new Error(
                            'Du behöver vara inloggad för att se dina innehav.',
                        );
                    }

                    if (response.status === 403) {
                        throw new Error(
                            'Du saknar behörighet att visa innehaven.',
                        );
                    }

                    throw new Error(
                        `Kunde inte hämta innehaven. HTTP ${response.status}.`,
                    );
                }

                // Parse the JSON response returned by the backend.
                const data =
                    (await response.json()) as HoldingsResponse;

                // Validate that the response contains a holdings array.
                if (!Array.isArray(data.holdings)) {
                    throw new Error(
                        'Backend returnerade ett oväntat svar.',
                    );
                }

                // Map backend field names to the existing frontend model.
                const mappedHoldings: HoldingViewItem[] =
                    data.holdings.map((holding) => {
                        const quantity = toNumber(holding.quantity);
                        const averageBuyPrice = toNumber(
                            holding.avg_buy_price,
                        );

                        const pnl = holding.pnl;

                        // Calculate the invested value using the average purchase price.
                        const investedValue =
                            quantity * averageBuyPrice;

                        // Calculate the percentage return when the invested value is available.
                        const returnPercent =
                            pnl !== null && investedValue > 0
                                ? (pnl / investedValue) * 100
                                : 0;

                        return {
                            id: String(holding.id),
                            symbol: holding.ticker ?? '',
                            name: holding.instrument_name ?? '',
                            shares: quantity,
                            value: toNumber(holding.marketValue),
                            returnPercent,
                            accountType: holding.account_type ?? '',
                            accountName: holding.account_name ?? '',
                            currency: holding.currency ?? 'SEK',
                            pnl,
                        };
                    });

                // Update the component state only if the request is still active.
                if (!controller.signal.aborted) {
                    setHoldings(mappedHoldings);
                }
            } catch (err) {
                // Ignore errors caused by intentionally cancelled requests.
                if (controller.signal.aborted) {
                    return;
                }

                console.error('Failed to fetch holdings:', err);

                // Display a user-friendly error message.
                setError(
                    err instanceof Error
                        ? err.message
                        : 'Ett oväntat fel inträffade när innehaven skulle hämtas.',
                );
            } finally {
                // Reset the loading state when the request finishes.
                if (!controller.signal.aborted) {
                    setLoading(false);
                }
            }
        };

        fetchHoldings();

        // Cancel the request if the component unmounts.
        return () => {
            controller.abort();
        };
    }, []);

    // Filters holdings by search query, account type, and currency.
    const filteredHoldings = useMemo(() => {
        const query = normalize(searchQuery);

        return holdings.filter((item) => {
            // Match the search query against the ticker symbol or instrument name.
            const matchesSearch =
                normalize(item.symbol).includes(query) ||
                normalize(item.name).includes(query);

            // Match the selected account against the account type or account name.
            const accountType = normalize(item.accountType);
            const accountName = normalize(item.accountName);

            const matchesAccount =
                selectedAccount === 'all' ||
                accountType === selectedAccount ||
                accountName.includes(selectedAccount);

            // Match the selected currency.
            const matchesCurrency =
                normalize(item.currency) === selectedCurrency;

            return (
                matchesSearch &&
                matchesAccount &&
                matchesCurrency
            );
        });
    }, [
        holdings,
        searchQuery,
        selectedAccount,
        selectedCurrency,
    ]);

    // Calculate the total market value of the filtered holdings.
    const totalValue = filteredHoldings.reduce(
        (total, item) => total + item.value,
        0,
    );

    // Calculate the total profit or loss reported by the backend.
    const totalReturn = filteredHoldings.reduce(
        (total, item) => total + (item.pnl ?? 0),
        0,
    );

    // Estimate the invested value from the market value and reported profit or loss.
    const totalInvested = filteredHoldings.reduce(
        (total, item) => total + item.value - (item.pnl ?? 0),
        0,
    );

    // Calculate the overall return percentage when the invested value is positive.
    const totalReturnPercent =
        totalInvested > 0
            ? (totalReturn / totalInvested) * 100
            : 0;

    // Select the appropriate CSS class for positive or negative returns.
    const returnClass =
        totalReturn >= 0 ? 'positive' : 'negative';

    return (
        <div className="holdings-container">
            <h1 className="holdings-title">Mina Innehav</h1>

            {/* Search field and filter controls. */}
            <div className="holdings-filter-section">
                <SearchInput
                    value={searchQuery}
                    onChange={setSearchQuery}
                />

                <div className="holdings-filter-buttons">
                    <DropdownBtn
                        options={ACCOUNT_OPTIONS}
                        value={selectedAccount}
                        onChange={setSelectedAccount}
                        placeholder="Välj konto"
                    />

                    <DropdownBtn
                        options={CURRENCY_OPTIONS}
                        value={selectedCurrency}
                        onChange={setSelectedCurrency}
                        placeholder="Valuta"
                    />
                </div>
            </div>

            {/* Summary cards displaying the current portfolio values. */}
            <div className="holdings-kpi-grid">
                <div className="holdings-kpi-card">
                    <span className="holdings-kpi-label">
                        Totalt värde
                    </span>

                    <div className="holdings-kpi-value">
                        {formatCurrency(totalValue)}
                    </div>

                    <div
                        className={`holdings-kpi-trend ${returnClass}`}
                    >
                        {totalReturnPercent >= 0 ? '+' : ''}
                        {totalReturnPercent.toLocaleString('sv-SE', {
                            maximumFractionDigits: 2,
                        })}
                        % avkastning
                    </div>
                </div>

                <div className="holdings-kpi-card">
                    <span className="holdings-kpi-label">
                        Avkastning (SEK)
                    </span>

                    <div className="holdings-kpi-value">
                        {totalReturn >= 0 ? '+' : ''}
                        {formatCurrency(totalReturn)}
                    </div>

                    <div
                        className={`holdings-kpi-trend ${returnClass}`}
                    >
                        {filteredHoldings.length} innehav
                    </div>
                </div>
            </div>

            {/* Holdings list with loading, error, and empty states. */}
            <div className="holdings-section-box">
                <h3 className="holdings-section-title">
                    Innehav
                </h3>

                {loading && (
                    <div
                        style={{
                            padding: '30px',
                            textAlign: 'center',
                            color: '#64748B',
                            fontSize: '14px',
                        }}
                    >
                        Laddar innehav...
                    </div>
                )}

                {error && (
                    <div
                        style={{
                            padding: '30px',
                            textAlign: 'center',
                            color: '#DC2626',
                            fontSize: '14px',
                        }}
                    >
                        {error}
                    </div>
                )}

                {!loading &&
                    !error &&
                    filteredHoldings.length === 0 && (
                        <div
                            style={{
                                padding: '30px',
                                textAlign: 'center',
                                color: '#64748B',
                                fontSize: '14px',
                            }}
                        >
                            Inga innehav hittades.
                        </div>
                    )}

                {!loading && !error && (
                    <div>
                        {filteredHoldings.map((item) => (
                            <HoldingRow
                                key={item.id}
                                item={item}
                                onClick={(id) =>
                                    console.log(
                                        'Clicked holding:',
                                        id,
                                    )
                                }
                            />
                        ))}
                    </div>
                )}
            </div>

            {/* Detailed summary of the filtered holdings. */}
            <div className="holdings-section-box">
                <h3 className="holdings-section-title">
                    Total innehav
                </h3>

                <div className="holdings-summary-content">
                    <div className="summary-row">
                        <span className="summary-label">
                            Värde totalt
                        </span>

                        <span className="summary-value">
                            {formatCurrency(totalValue)}
                        </span>
                    </div>

                    <div className="summary-row">
                        <span className="summary-label">
                            Avkastning (SEK)
                        </span>

                        <span
                            className={`summary-value ${returnClass}`}
                        >
                            {totalReturn >= 0 ? '+' : ''}
                            {formatCurrency(totalReturn)}
                        </span>
                    </div>

                    <div
                        className="summary-row"
                        style={{ borderBottom: 'none' }}
                    >
                        <span className="summary-label">
                            Avkastning (%)
                        </span>

                        <span
                            className={`summary-value ${returnClass}`}
                        >
                            {totalReturnPercent.toLocaleString(
                                'sv-SE',
                                {
                                    maximumFractionDigits: 2,
                                },
                            )}
                            %
                        </span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Holdings;