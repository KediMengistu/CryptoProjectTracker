// components/SkeletonCoinGrid.tsx
"use client";

import React from "react";
import { AnimatePresence, motion } from "framer-motion";
import { Card } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import GridTailImage from "@/assets/CoinGecko_Logo.webp";
import Image from "next/image";

const PLACEHOLDER_COUNT = 15;

export default function SkeletonCoinGrid() {
  return (
    <div className="pb-2">
      {/* bottom padding so the image has breathing room */}
      <div
        className="
          grid gap-4
          grid-cols-1
          sm:grid-cols-3
          lg:grid-cols-5
        "
      >
        <AnimatePresence initial={true}>
          {Array.from({ length: PLACEHOLDER_COUNT }).map((_, idx) => (
            <motion.div
              key={idx}
              initial={{ opacity: 0, y: 6 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0 }}
              transition={{ duration: 0.18, ease: "easeOut" }}
            >
              <Card
                className="
                  relative h-full w-full overflow-hidden p-4
                  bg-[conic-gradient(at_right,_var(--tw-gradient-stops))]
                  from-stone-100 via-neutral-900 to-neutral-900
                "
              >
                {/* local noise overlay */}
                <div
                  className="
                    pointer-events-none absolute inset-0
                    bg-[url('/noise.svg')]
                    opacity-25 brightness-100 contrast-150
                  "
                />
                <div className="relative z-10">
                  <div className="flex flex-col space-y-3">
                    {/* Hero media area */}
                    <Skeleton className="h-[200px] w-full rounded-xl" />

                    {/* Article meta + lines */}
                    <div className="space-y-2">
                      <div className="flex items-center space-x-2">
                        <Skeleton className="h-8 w-8 rounded-full" />
                        <Skeleton className="h-4 w-24" />
                      </div>

                      <Skeleton className="h-5 w/full" />
                      <Skeleton className="h-4 w-4/5" />
                      <Skeleton className="h-4 w-3/5" />
                    </div>
                  </div>
                </div>
              </Card>
            </motion.div>
          ))}
        </AnimatePresence>
      </div>

      {/* Footer attribution (persistent, not animated) */}
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
