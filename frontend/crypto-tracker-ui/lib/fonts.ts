// lib/fonts.ts
import { Roboto_Mono } from "next/font/google";

/**
 * Use Roboto Mono for EVERYTHING.
 * We bind it to both --font-sans and --font-mono so `font-sans`
 * and `font-mono` resolve to the same face app-wide.
 */
export const fontSans = Roboto_Mono({
  subsets: ["latin"],
  display: "swap",
  variable: "--font-sans",
  weight: ["300", "400", "500", "700"],
});

export const fontMono = Roboto_Mono({
  subsets: ["latin"],
  display: "swap",
  variable: "--font-mono",
  weight: ["300", "400", "500", "700"],
});

// (Optional) If you used a heading font before, point it to Roboto Mono too:
export const fontHeading = Roboto_Mono({
  subsets: ["latin"],
  display: "swap",
  variable: "--font-heading",
  weight: ["500", "700"],
});

export const fontVars = [
  fontSans.variable,
  fontMono.variable,
  fontHeading.variable,
].join(" ");
