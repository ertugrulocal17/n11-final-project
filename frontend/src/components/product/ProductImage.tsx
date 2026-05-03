import { cn } from '@/lib/utils'
import { ImageOff } from 'lucide-react'
import { useState } from 'react'

type Props = {
  src?: string | null
  alt: string
  className?: string
  fallbackLetter: string
  imgClassName?: string
}

/** Ürün görseli; yükleme hatası veya URL yoksa harf / ikon ile yer tutucu. */
export function ProductImage({ src, alt, className, fallbackLetter, imgClassName }: Props) {
  const [failed, setFailed] = useState(false)
  const letter = fallbackLetter.slice(0, 1).toUpperCase() || '?'

  if (!src || failed) {
    return (
      <div
        className={cn(
          'flex items-center justify-center bg-gradient-to-br from-muted via-card to-accent/35 text-primary/30',
          className,
        )}
      >
        {src && failed ? (
          <ImageOff className="size-10 opacity-40" strokeWidth={1.25} aria-hidden />
        ) : (
          <span className="font-display text-4xl font-bold tracking-tight sm:text-5xl">{letter}</span>
        )}
      </div>
    )
  }

  return (
    <div className={cn('relative overflow-hidden bg-muted', className)}>
      <img
        src={src}
        alt={alt}
        loading="lazy"
        decoding="async"
        className={cn('size-full object-cover transition duration-300 hover:scale-[1.02]', imgClassName)}
        onError={() => setFailed(true)}
      />
    </div>
  )
}
