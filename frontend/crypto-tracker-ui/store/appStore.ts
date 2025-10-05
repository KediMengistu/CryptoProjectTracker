import { create } from "zustand";
import { devtools, persist } from "zustand/middleware";
import { CoinSlice, createCoinSlice } from "../slices/coinSlice";

export type AppState = CoinSlice;

// Root store (composed from slices)
export const useAppStore = create<AppState>()(
  devtools(
    persist(
      (...a) => ({
        ...createCoinSlice(...a),
      }),
      { name: "app-store" }
    ),
    {
      name: "app-store",
      anonymousActionType: "zustand:setState",
    }
  )
);

// Convenience selectors
export const useFeed = () => useAppStore((s: any) => s.coins);
export const useFeedStatus = () => useAppStore((s: any) => s.status);
export const useFeedError = () => useAppStore((s: any) => s.error);
