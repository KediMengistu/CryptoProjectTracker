// app/_components/GridTailImage.tsx
import Image, { type StaticImageData } from "next/image";

type GridTailImageProps = {
  src?: string | StaticImageData; // <-- accept both
  alt?: string;
  width?: number;
  height?: number;
  className?: string;
};

export default function GridTailImage({
  src = "/brand-mark.png",
  alt = "Brand mark",
  width = 96,
  height = 96,
  className = "",
}: GridTailImageProps) {
  return (
    <div
      className={`w-full flex justify-center items-center mt-8 mb-6 ${className}`}
    >
      <Image
        src={src}
        alt={alt}
        width={width}
        height={height}
        className="opacity-80"
      />
    </div>
  );
}
