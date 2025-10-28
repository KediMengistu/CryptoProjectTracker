// slices/coinSlice.ts
import type { StateCreator } from "zustand";
import type { AppState } from "../store/appStore";
import type { CoinItem } from "./coinDTO";

export interface CoinSliceState {
  coins: CoinItem[];
  status: "idle" | "loading" | "succeeded" | "failed";
  error?: string;
}

export interface CoinSliceActions {
  /** Fetches only the composed feed from `/api/coins/feed`. */
  fetchFeed: (opts?: { signal?: AbortSignal }) => Promise<void>;
  clearError: () => void;
}

export type CoinSlice = CoinSliceState & CoinSliceActions;

// Read at build time (Next.js inlines this for the client bundle)
const API_BASE = process.env.NEXT_PUBLIC_API_BASE;

export const createCoinSlice: StateCreator<
  AppState,
  [["zustand/devtools", never], ["zustand/persist", unknown]],
  [],
  CoinSlice
> = (set, get) => ({
  coins: [],
  status: "idle",
  error: undefined,

  fetchFeed: async (opts) => {
    const { status } = get();

    // If a previous render left us in "loading" (shouldn't persist anymore),
    // we'll still allow a new request if the caller provided a fresh signal
    // and explicitly wants to refetch. Keeping this simple: we keep the guard,
    // as it’s now safe with non-persisted status.
    if (status === "loading") return;

    // Flip to loading
    set({ status: "loading", error: undefined }, false, "coin/fetchFeed:start");

    try {
      if (!API_BASE) {
        // Be explicit so the UI doesn't hang: surface a real error state.
        const msg =
          "NEXT_PUBLIC_API_BASE is not set. Configure it in your Vercel project settings.";
        set({ status: "failed", error: msg }, false, "coin/fetchFeed:missingBase");
        return;
      }

      const res = await fetch(`${API_BASE}/api/coins/feed`, {
        method: "GET",
        headers: { Accept: "application/json" },
        cache: "no-store",
        signal: opts?.signal,
      });

      if (!res.ok) {
        const message = `Feed request failed: ${res.status} ${res.statusText}`;
        set({ status: "failed", error: message }, false, "coin/fetchFeed:failed");
        return;
      }

      const data = (await res.json()) as CoinItem[];
      set(
        {
          coins: Array.isArray(data) ? data : [],
          status: "succeeded",
          error: undefined,
        },
        false,
        "coin/fetchFeed:success"
      );
    } catch (err: any) {
      if (err?.name === "AbortError") {
        set({ status: "idle" }, false, "coin/fetchFeed:aborted");
        return;
      }
      set(
        { status: "failed", error: err?.message ?? "Unknown error" },
        false,
        "coin/fetchFeed:failed:exception"
      );
    }
  },

  clearError: () => set({ error: undefined }, false, "coin/clearError"),
});
