"use client";

import { memo } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { Card } from "@/components/ui/card";
import Image from "next/image";
import type { CoinItem } from "@/slices/coinDTO";
import { coinImageMap } from "@/slices/coinDTO";
import GridTailImage from "@/assets/CoinGecko_Logo.webp";

interface IntroDataCoinGridProps {
  coins: CoinItem[];
}

/**
 * IntroDataCoinGrid
 * - Matches SkeletonCoinGrid layout (grid and card spacing)
 * - Image area: 200px height & rounded-xl; transparent bg in light mode, subtle bg in dark
 * - Text: black in light mode, white in dark mode
 * - Pointer cursor on hover
 * - Subtle hover tint: darker in light mode, lighter in dark mode
 * - CoinGecko attribution footer
 * - Badge label: "TRENDING" or "CLASSIC" (instead of FALLBACK)
 */
function IntroDataCoinGridComponent({ coins }: IntroDataCoinGridProps) {
  return (
    <div className="pb-2">
      <div
        className="
          grid gap-4
          grid-cols-1
          sm:grid-cols-3
          lg:grid-cols-5
        "
      >
        <AnimatePresence initial={true}>
          {coins?.map((c) => {
            const specific = coinImageMap[c.coinGeckoId];
            const isTrending = c.source === "TRENDING";
            const imgSrc =
              isTrending && specific
                ? specific
                : isTrending && coinImageMap.trending
                ? coinImageMap.trending
                : specific ?? coinImageMap.trending;

            const repoCount = Array.isArray(c.repoUrls) ? c.repoUrls.length : 0;
            const repoBadge = `+${repoCount}`;
            const sourceLabel = c.source === "FALLBACK" ? "Classic" : c.source;

            return (
              <motion.div
                key={c.coinGeckoId}
                initial={{ opacity: 0, y: 6 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0 }}
                transition={{ duration: 0.18, ease: "easeOut" }}
              >
                <Card
                  className="
                    group relative h-full w-full overflow-hidden p-4
                    bg-[conic-gradient(at_right,_var(--tw-gradient-stops))]
                    from-stone-100 via-neutral-900 to-neutral-900
                    hover:cursor-pointer
                  "
                >
                  {/* HOVER TINT OVERLAY (fades in on hover) */}
                  <div
                    className="
                      absolute inset-0 z-0
                      opacity-0 transition-opacity duration-200
                      group-hover:opacity-100
                      bg-black/[0.05] dark:bg-white/[0.08]
                    "
                    aria-hidden
                  />

                  {/* local noise overlay (beneath content, above gradient) */}
                  <div
                    className="
                      pointer-events-none absolute inset-0 z-0
                      bg-[url('/noise.svg')]
                      opacity-25 brightness-100 contrast-150
                    "
                    aria-hidden
                  />

                  {/* content */}
                  <div className="relative z-10">
                    <div className="flex flex-col space-y-3">
                      {/* Media area: fixed height; transparent in light, subtle in dark */}
                      <div
                        className="
                          relative h-[200px] w-full overflow-hidden rounded-xl
                          flex items-center justify-center
                          bg-transparent dark:bg-black/10
                        "
                      >
                        <Image
                          src={imgSrc}
                          alt={`${c.symbol} - ${c.name}`}
                          fill
                          sizes="(max-width: 640px) 100vw, (max-width: 1024px) 33vw, 20vw"
                          className="object-contain object-center"
                          priority={false}
                        />
                      </div>

                      {/* Text section */}
                      <div className="space-y-2">
                        {/* Title line: SYMBOL - Name */}
                        <div className="text-base md:text-lg font-semibold tracking-tight text-black/90 dark:text-white/90">
                          {`${c.symbol} - ${c.name}`}
                        </div>

                        {/* Brief info: Source • +N repos */}
                        <div className="text-sm text-black/80 dark:text-white/80">
                          <span className="inline-flex items-center gap-1">
                            <span className="rounded-full bg-black/10 dark:bg-white/10 px-2 py-0.5 text-xs uppercase tracking-wide">
                              {sourceLabel}
                            </span>
                            <span aria-hidden="true">•</span>
                            <span className="font-medium">{repoBadge}</span>
                            <span className="opacity-80">repos</span>
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                </Card>
              </motion.div>
            );
          })}
        </AnimatePresence>
      </div>

      {/* Footer attribution (same as SkeletonCoinGrid) */}
      <div className="mt-4 flex items-center justify-center gap-2 text-sm text-muted-foreground">
        <Image
          src={GridTailImage}
          alt="CoinGecko logo"
          width={40}
          height={40}
          className="opacity-90"
        />
        <span>
          Powered by{" "}
          <a
            href="https://www.coingecko.com/"
            target="_blank"
            rel="noopener noreferrer"
            className="font-medium text-foreground underline-offset-4 hover:underline"
          >
            CoinGecko
          </a>
        </span>
      </div>
    </div>
  );
}

const IntroDataCoinGrid = memo(IntroDataCoinGridComponent);
export default IntroDataCoinGrid;
