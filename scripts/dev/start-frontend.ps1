Set-Location frontend/web-console
if (Get-Command pnpm -ErrorAction SilentlyContinue) {
  pnpm dev
} else {
  npm run dev
}
