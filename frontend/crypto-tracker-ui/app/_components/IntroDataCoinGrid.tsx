"use client";

import React, { memo, useEffect, useId, useRef, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { Card } from "@/components/ui/card";
import Image from "next/image";
import type { CoinItem } from "@/slices/coinDTO";
import { coinImageMap } from "@/slices/coinDTO";
import CoinGeckoLogo from "@/assets/CoinGecko_Logo.webp";
import { cn } from "@/lib/utils";
import { useOutsideClick } from "@/hooks/use-outside-click";
import { RefreshCcw } from "lucide-react";

interface IntroDataCoinGridProps {
  coins: CoinItem[];
  onRefresh?: () => void; // wired from Home
}

type ActiveCoin = CoinItem | null;

function IntroDataCoinGridComponent({
  coins,
  onRefresh,
}: IntroDataCoinGridProps) {
  const [active, setActive] = useState<ActiveCoin>(null);
  const idScope = useId();
  const modalRef = useRef<HTMLDivElement>(
    null
  ) as React.RefObject<HTMLDivElement>;

  useOutsideClick(modalRef, () => setActive(null));

  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) =>
      e.key === "Escape" && setActive(null);
    window.addEventListener("keydown", onKeyDown);
    document.body.style.overflow = active ? "hidden" : "auto";
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [active]);

  const LID = (part: string, id: string) => `${part}-${id}-${idScope}`;

  return (
    <div className="pb-1">
      {/* REFRESH BUTTON OVERLAY + GRID */}
      <div className="relative">
        {/* Absolutely positioned refresh overlaying the grid's top-left */}
        <button
          type="button"
          aria-label="Refresh feed"
          onClick={(e) => {
            e.preventDefault();
            e.stopPropagation();
            onRefresh?.();
          }}
          onMouseDown={(e) => {
            e.preventDefault();
            e.stopPropagation();
          }}
          className={cn(
            "absolute left-2 top-2 z-50 pointer-events-auto inline-flex items-center gap-2 rounded-full px-3 py-2 text-sm",
            // base
            "bg-white text-black dark:bg-neutral-900 dark:text-white",
            "border border-border shadow-sm cursor-pointer",
            // inverse-on-hover (match close button behavior)
            "transition-colors duration-200",
            "hover:bg-neutral-900 hover:text-white",
            "dark:hover:bg-white dark:hover:text-black",
            // focus ring
            "focus-visible:ring-2 focus-visible:ring-ring"
          )}
        >
          <RefreshCcw className="h-4 w-4" />
          Refresh
        </button>

        {/* GRID */}
        <div className="grid gap-4 grid-cols-1 sm:grid-cols-3 lg:grid-cols-5">
          <AnimatePresence initial>
            {coins?.map((c) => {
              const specific = coinImageMap[c.coinGeckoId];
              const isTrending = c.source === "TRENDING";
              const imgSrc =
                isTrending && specific
                  ? specific
                  : isTrending && coinImageMap.trending
                  ? coinImageMap.trending
                  : specific ?? coinImageMap.trending;

              const repoCount = Array.isArray(c.repoUrls)
                ? c.repoUrls.length
                : 0;
              const repoBadge = `+${repoCount}`;
              const sourceLabel =
                c.source === "FALLBACK" ? "Classic" : c.source;
              const titleText = `${(c.symbol ?? "").toUpperCase()} - ${c.name}`;

              return (
                <motion.div
                  key={c.coinGeckoId}
                  initial={{ opacity: 0, y: 6 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0 }}
                  transition={{ duration: 0.18, ease: "easeOut" }}
                >
                  <motion.button
                    type="button"
                    onClick={() => setActive(c)}
                    layoutId={LID("card", c.coinGeckoId)}
                    className={cn(
                      "w-full text-left group relative overflow-hidden p-4 rounded-xl",
                      // Solid base + gradient + border + shadow (match skeleton)
                      "bg-white dark:bg-neutral-950",
                      "border border-border shadow-sm",
                      "bg-[conic-gradient(at_right,_var(--tw-gradient-stops))] from-stone-100 via-neutral-900 to-neutral-900",
                      "hover:cursor-pointer focus:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                    )}
                  >
                    {/* Hover tint */}
                    <div className="absolute inset-0 z-0 opacity-0 transition-opacity duration-200 group-hover:opacity-100 bg-black/[0.05] dark:bg-white/[0.08]" />
                    {/* Noise */}
                    <div className="pointer-events-none absolute inset-0 z-0 bg-[url('/noise.svg')] opacity-25 brightness-100 contrast-150" />
                    {/* Content */}
                    <div className="relative z-10">
                      <div className="flex flex-col space-y-3">
                        <motion.div
                          layoutId={LID("image", c.coinGeckoId)}
                          className="relative h-[200px] w-full overflow-hidden rounded-xl flex items-center justify-center bg-transparent dark:bg-black/10"
                        >
                          <Image
                            src={imgSrc}
                            alt={titleText}
                            fill
                            sizes="(max-width: 640px) 100vw, (max-width: 1024px) 33vw, 20vw"
                            className="object-contain object-center"
                          />
                        </motion.div>

                        <div className="space-y-2">
                          <motion.div
                            layoutId={LID("title", c.coinGeckoId)}
                            className="text-base md:text-lg font-semibold tracking-tight text-black/90 dark:text-white/90"
                          >
                            {titleText}
                          </motion.div>

                          <motion.div
                            layoutId={LID("desc", c.coinGeckoId)}
                            className="text-sm text-black/80 dark:text-white/80"
                          >
                            <span className="inline-flex items-center gap-1">
                              <span className="rounded-full bg-black/10 dark:bg-white/10 px-2 py-0.5 text-xs uppercase tracking-wide">
                                {sourceLabel}
                              </span>
                              <span aria-hidden="true">•</span>
                              <span className="font-medium">{repoBadge}</span>
                              <span className="opacity-80">repos</span>
                            </span>
                          </motion.div>
                        </div>
                      </div>
                    </div>
                  </motion.button>
                </motion.div>
              );
            })}
          </AnimatePresence>
        </div>
      </div>

      {/* Footer attribution as a Card with same styling & noise */}
      <Card
        className="
          relative mt-4 overflow-hidden rounded-xl p-3 sm:p-4
          bg-white dark:bg-neutral-950
          border border-border shadow-sm
          bg-[conic-gradient(at_right,_var(--tw-gradient-stops))]
          from-stone-100 via-neutral-900 to-neutral-900
        "
      >
        <div
          className="
            pointer-events-none absolute inset-0
            bg-[url('/noise.svg')]
            opacity-25 brightness-100 contrast-150
          "
        />
        <div className="relative z-10 flex flex-col items-center justify-center gap-1">
          <div className="flex items-center justify-center gap-2 text-sm">
            <Image
              src={CoinGeckoLogo}
              alt="CoinGecko logo"
              width={40}
              height={40}
              className="opacity-90"
            />
            <span className="text-foreground">
              Powered by{" "}
              <a
                href="https://www.coingecko.com/"
                target="_blank"
                rel="noopener noreferrer"
                className="font-medium underline-offset-4 hover:underline"
              >
                CoinGecko.
              </a>
            </span>
          </div>
          <p className="text-xs text-muted-foreground">
            Data is fetched hourly @ HH:05. Click refresh to view updated data.
          </p>
        </div>
      </Card>

      {/* OVERLAY + MODAL */}
      <AnimatePresence>
        {active && (
          <>
            {/* Backdrop blocks interactions with the grid (including refresh button) */}
            <motion.div
              key="backdrop"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="fixed inset-0 bg-black/20 backdrop-blur-[1px] z-40"
              onClick={() => setActive(null)}
            />
            <div className="fixed inset-0 z-50 grid place-items-center p-3 sm:p-4">
              <motion.div
                ref={modalRef}
                layoutId={LID("card", active.coinGeckoId)}
                className="relative w-full max-w-[560px] max-h-[92vh] h-[92vh] md:h-auto md:max-h-[92vh] flex flex-col bg-white dark:bg-neutral-900 rounded-3xl overflow-hidden shadow-2xl ring-1 ring-border"
              >
                {/* Close button with hover inversion */}
                <motion.button
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  exit={{ opacity: 0, transition: { duration: 0.05 } }}
                  className={cn(
                    "absolute top-3 right-3 z-10 flex items-center justify-center rounded-full h-8 w-8 shadow",
                    "cursor-pointer transition-colors duration-200",
                    "bg-white text-black dark:bg-neutral-800 dark:text-white",
                    "hover:bg-neutral-900 hover:text-white",
                    "dark:hover:bg-white dark:hover:text-black",
                    "focus-visible:ring-2 focus-visible:ring-ring"
                  )}
                  onClick={() => setActive(null)}
                  aria-label="Close"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    className="h-4 w-4"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                  >
                    <path d="M18 6L6 18" />
                    <path d="M6 6l12 12" />
                  </svg>
                </motion.button>

                {/* Image */}
                <motion.div layoutId={LID("image", active.coinGeckoId)}>
                  <div className="relative w-full h-72 bg-neutral-50 dark:bg-neutral-900">
                    <Image
                      src={
                        coinImageMap[active.coinGeckoId] ??
                        coinImageMap.trending
                      }
                      alt={`${(active.symbol ?? "").toUpperCase()} - ${
                        active.name
                      }`}
                      fill
                      className="object-contain object-center"
                      priority
                    />
                  </div>
                </motion.div>

                {/* Header */}
                <div className="flex items-start justify-between gap-3 p-4">
                  <div>
                    <motion.h3
                      layoutId={LID("title", active.coinGeckoId)}
                      className="font-semibold text-neutral-800 dark:text-neutral-100 text-lg"
                    >
                      {(active.symbol ?? "").toUpperCase()} – {active.name}
                    </motion.h3>
                    <motion.p
                      layoutId={LID("desc", active.coinGeckoId)}
                      className="text-neutral-600 dark:text-neutral-400 text-sm"
                    >
                      {active.source === "FALLBACK" ? "Classic" : active.source}{" "}
                      •{" "}
                      {Array.isArray(active.repoUrls)
                        ? `+${active.repoUrls.length} repos`
                        : "+0 repos"}
                    </motion.p>
                  </div>
                </div>

                {/* Scrollable details */}
                <div className="flex-1 min-h-0 px-4 pb-4 overflow-y-auto scrollbar-hide">
                  <motion.div
                    layout
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    className="text-neutral-700 dark:text-neutral-300 text-sm md:text-[15px] leading-6"
                  >
                    <div className="grid grid-cols-2 gap-3 mb-4">
                      {[
                        ["Stars", active.stars],
                        ["Forks", active.forks],
                        ["Watchers", active.subscribers],
                        ["PRs merged", active.pullRequestsMerged],
                        ["PR contributors", active.pullRequestContributors],
                        ["Issues", active.totalIssues],
                        ["Closed issues", active.closedIssues],
                        ["Commits (4w)", active.commitCount4w],
                        ["Additions (4w)", active.codeAdditions4w],
                        [
                          "Deletions (4w)",
                          typeof active.codeDeletions4w === "number"
                            ? Math.abs(active.codeDeletions4w)
                            : active.codeDeletions4w,
                        ],
                      ].map(([label, value]) => (
                        <div
                          key={label as string}
                          className="rounded-lg border border-border p-3 bg-background/40"
                        >
                          <div className="text-xs text-muted-foreground">
                            {label}
                          </div>
                          <div className="text-base font-medium">
                            {value ?? "—"}
                          </div>
                        </div>
                      ))}
                    </div>

                    <div className="space-y-2 pb-2">
                      <div className="text-xs uppercase tracking-wide text-muted-foreground">
                        Repository Links
                      </div>
                      <ul className="space-y-1.5">
                        {(active.repoUrls ?? []).map((u) => (
                          <li key={u}>
                            <a
                              href={u}
                              target="_blank"
                              rel="noreferrer"
                              className="underline underline-offset-4 hover:opacity-80 break-all"
                            >
                              {u}
                            </a>
                          </li>
                        ))}
                        {(!active.repoUrls || active.repoUrls.length === 0) && (
                          <li className="text-muted-foreground">None found.</li>
                        )}
                      </ul>
                    </div>
                  </motion.div>
                </div>
              </motion.div>
            </div>
          </>
        )}
      </AnimatePresence>
    </div>
  );
}

const IntroDataCoinGrid = memo(IntroDataCoinGridComponent);
export default IntroDataCoinGrid;
