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

  const startMinDelay = () => {
    if (timerRef.current) window.clearTimeout(timerRef.current);
    setMinDelayElapsed(false);
    timerRef.current = window.setTimeout(() => setMinDelayElapsed(true), 2000);
  };

  useEffect(() => {
    // initial fetch
    abortRef.current = new AbortController();
    fetchFeed({ signal: abortRef.current.signal }).catch(() => void 0);
    startMinDelay();

    return () => {
      if (timerRef.current) window.clearTimeout(timerRef.current);
      abortRef.current?.abort();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleRefresh = () => {
    // abort any in-flight request and re-fetch
    abortRef.current?.abort();
    abortRef.current = new AbortController();
    startMinDelay();
    fetchFeed({ signal: abortRef.current.signal }).catch(() => void 0);
  };

  const showDataGrid = minDelayElapsed && status !== "loading";

  return (
    <>
      <Card className="h-full w-full bg-transparent hover:cursor-default outline-none border-none shadow-none overflow-y-auto rounded-none px-2 scrollbar-hide">
        {!showDataGrid ? (
          <SkeletonCoinGrid />
        ) : (
          <IntroDataCoinGrid coins={coins} onRefresh={handleRefresh} />
        )}
      </Card>
    </>
  );
}
