/* ---------- Image map for coin cards ---------- */
import type { StaticImageData } from "next/image";

/* Local assets (as listed in your /assets directory) */
import AptosImg from "@/assets/Aptos_classic_coin.png";
import ArbitrumImg from "@/assets/Arbitrum_classic_coin.png";
import Avalanche2Img from "@/assets/Avalanche-2_classic_coin.png";
import BitcoinImg from "@/assets/Bitcoin_classic_coin.png";
import ChainlinkImg from "@/assets/Chainlink_classic_coin.png";
import CardanoImg from "@/assets/Cordano_classic_coin.png"; // file name "Cordano", coin id is "cardano"
import CosmosImg from "@/assets/Cosmos_classic_coin.png";
import DogeImg from "@/assets/Doge_classic_coin.png";
import EthereumImg from "@/assets/Ethereum_classic_coin.png";
import InternetComputerImg from "@/assets/Internet-computer_classic_coin.png";
import LitecoinImg from "@/assets/Litecoin_classic_coin.png";
import NearImg from "@/assets/Near_classic_coin.png";
import PolkadotImg from "@/assets/Polkadot_classic_coin.png";
import SolanaImg from "@/assets/Solana_classic_coin.png";
import SuiImg from "@/assets/Sui_classic_coin.png";
import TrendingImg from "@/assets/Trending_coin.png";

export interface CoinImageMap {
  /** Fallback image for any TRENDING coin (or unknown coin) without a specific mapping */
  trending: StaticImageData;
  /** Per-coinGeckoId image entries (e.g., bitcoin, ethereum, solana, etc.) */
  [coinGeckoId: string]: StaticImageData;
}

export const coinImageMap: CoinImageMap = {
  trending: TrendingImg,

  // exact CoinGecko ids:
  bitcoin: BitcoinImg,
  ethereum: EthereumImg,
  solana: SolanaImg,
  "avalanche-2": Avalanche2Img,
  cardano: CardanoImg,
  dogecoin: DogeImg,
  polkadot: PolkadotImg,
  chainlink: ChainlinkImg,
  cosmos: CosmosImg,
  litecoin: LitecoinImg,
  "internet-computer": InternetComputerImg,
  near: NearImg,
  arbitrum: ArbitrumImg,
  aptos: AptosImg,
  sui: SuiImg,
};

export type Source = "TRENDING" | "FALLBACK";

/** Matches GET /api/coins/feed item shape */
export interface CoinItem {
  coinGeckoId: string;
  symbol: string;
  name: string;
  repoUrls: string[];
  forks: number | null;
  stars: number | null;
  subscribers: number | null;
  totalIssues: number | null;
  closedIssues: number | null;
  pullRequestsMerged: number | null;
  pullRequestContributors: number | null;
  codeAdditions4w: number | null;
  codeDeletions4w: number | null;
  commitCount4w: number | null;
  snapshotDate: string; // e.g., "2025-10-04"
  source: Source;
  stale: boolean;
}
