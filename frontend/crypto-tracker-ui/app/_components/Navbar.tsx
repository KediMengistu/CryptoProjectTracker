// app/_components/Navbar.tsx
import React from "react";
import { cn } from "@/lib/utils";
import { Card } from "@/components/ui/card";
import { CiBitcoin } from "react-icons/ci";
import ModeToggle from "./ModeToggle";

function Navbar() {
  return (
    <Card
      className={cn(
        "sticky z-10 top-0 left-0 w-full h-fit",
        "grid grid-cols-3 items-center",
        "p-2",
        "rounded-none shadow-none outline-none dark:bg-neutral-950",
        "border-l-0 border-r-0"
      )}
    >
      <div></div>
      <div className="flex items-center justify-center">
        <CiBitcoin className="h-8 w-8" />
      </div>
      <div className="flex items-center justify-end">
        <ModeToggle className="scale-75" />
      </div>
    </Card>
  );
}

export default Navbar;
