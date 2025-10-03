// app/_components/ModeToggle.tsx
"use client";

import * as React from "react";
import { useTheme } from "next-themes";
import { Sun, Moon } from "lucide-react";
import { motion } from "motion/react";
import { cn } from "@/lib/utils";

const THEMES = [
  { key: "light", icon: Sun, label: "Light theme" },
  { key: "dark", icon: Moon, label: "Dark theme" },
] as const;

type ThemeKey = (typeof THEMES)[number]["key"];

export default function ModeToggle({ className }: { className?: string }) {
  const { theme, setTheme, resolvedTheme } = useTheme();
  const [mounted, setMounted] = React.useState(false);

  React.useEffect(() => setMounted(true), []);
  if (!mounted) return null;

  const active = (theme ?? resolvedTheme ?? "light") as ThemeKey;

  return (
    <div
      className={cn(
        // added cursor-pointer + select-none
        "relative isolate flex h-8 rounded-full bg-background p-1 ring-1 ring-border cursor-pointer select-none",
        className
      )}
    >
      {THEMES.map(({ key, icon: Icon, label }) => {
        const isActive = active === key;
        return (
          <button
            key={key}
            type="button"
            aria-label={label}
            // also cursor-pointer on each button
            className="relative h-6 w-6 rounded-full cursor-pointer"
            onClick={() => setTheme(key)}
          >
            {isActive && (
              <motion.div
                layoutId="activeTheme"
                className="absolute inset-0 rounded-full bg-secondary"
                transition={{ type: "spring", duration: 0.5 }}
              />
            )}
            <Icon
              className={cn(
                "relative z-10 m-auto h-4 w-4",
                isActive ? "text-foreground" : "text-muted-foreground"
              )}
            />
          </button>
        );
      })}
    </div>
  );
}
