import { create } from "zustand";
import { devtools, persist } from "zustand/middleware";
import { CoinSlice, createCoinSlice } from "../slices/coinSlice";

export type AppState = CoinSlice;

// Only persist the data we actually want to survive reloads.
// Critically: do NOT persist `status` or `error`, otherwise the app can get stuck in "loading".
export const useAppStore = create<AppState>()(
  devtools(
    persist(
      (...a) => ({
        ...createCoinSlice(...a),
      }),
      {
        name: "app-store",
        // Keep your data, drop volatile UI state
        partialize: (state) => ({
          coins: state.coins,
          // status and error intentionally excluded
        }),
        // Optional: give devtools a friendly action name on rehydrate
        version: 1,
      }
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
