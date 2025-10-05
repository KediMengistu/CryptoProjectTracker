"use client";

import { useEffect, useRef, useState } from "react";
import { Card } from "@/components/ui/card";
import SkeletonCoinGrid from "@/app/_components/SkeletonCoinGrid";
import IntroDataCoinGrid from "@/app/_components/IntroDataCoinGrid";
import { useAppStore, useFeed, useFeedStatus } from "@/store/appStore";

export default function Home() {
  const coins = useFeed();
  const status = useFeedStatus();
  const fetchFeed = useAppStore((s) => s.fetchFeed);

  // Minimum skeleton display
  const [minDelayElapsed, setMinDelayElapsed] = useState(false);
  const timerRef = useRef<number | null>(null);
  const abortRef = useRef<AbortController | null>(null);

  useEffect(() => {
    // fire API call
    abortRef.current = new AbortController();
    fetchFeed({ signal: abortRef.current.signal }).catch(() => void 0);

    // enforce 2s minimum skeleton
    timerRef.current = window.setTimeout(() => {
      setMinDelayElapsed(true);
    }, 2000);

    return () => {
      if (timerRef.current) window.clearTimeout(timerRef.current);
      abortRef.current?.abort();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // run once on mount

  const showDataGrid = minDelayElapsed && status !== "loading";

  return (
    <>
      <Card className="h-full w-full bg-transparent hover:cursor-default outline-none border-none shadow-none overflow-y-auto rounded-none px-2 scrollbar-hide">
        {!showDataGrid ? (
          <SkeletonCoinGrid />
        ) : (
          <IntroDataCoinGrid coins={coins} />
        )}
      </Card>
    </>
  );
}
