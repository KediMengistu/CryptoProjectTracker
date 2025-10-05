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

const API_BASE =
  process.env.NEXT_PUBLIC_API_BASE?.replace(/\/+$/, "") ||
  "http://localhost:8080";

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
    if (status === "loading") return;

    // coin/fetchFeed:start
    set({ status: "loading", error: undefined }, false, "coin/fetchFeed:start");

    try {
      const res = await fetch(`${API_BASE}/api/coins/feed`, {
        method: "GET",
        headers: { Accept: "application/json" },
        signal: opts?.signal,
      });

      if (!res.ok) {
        const message = `Feed request failed: ${res.status} ${res.statusText}`;
        // coin/fetchFeed:failed
        set(
          { status: "failed", error: message },
          false,
          "coin/fetchFeed:failed"
        );
        return;
      }

      const data = (await res.json()) as CoinItem[];

      // coin/fetchFeed:success
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
        // coin/fetchFeed:aborted
        set({ status: "idle" }, false, "coin/fetchFeed:aborted");
        return;
      }
      // coin/fetchFeed:failed:exception
      set(
        { status: "failed", error: err?.message ?? "Unknown error" },
        false,
        "coin/fetchFeed:failed:exception"
      );
    }
  },

  clearError: () =>
    // coin/clearError
    set({ error: undefined }, false, "coin/clearError"),
});
