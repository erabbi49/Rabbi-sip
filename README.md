# Samsung Phone 15.9.72 — Morphe patch experiment

This repository contains an experimental Stage 1 Morphe patch for
`com.samsung.android.dialer` version `15.9.72`.

## What it changes

It matches the startup call in `Pf.c.<clinit>()` to
`android.os.SemSystemProperties.get("ro.product_ship", "none")`, replaces
that call with the production-build string `"true"`, and removes the now
unneeded `move-result-object`.

This is intended to bypass the first reported non-One UI startup crash only.
It does not emulate other Samsung framework APIs, and does not establish
that contacts, IMS/VoLTE, SIM management, incoming calls or outgoing calls
will work.

## Build

GitHub Actions builds this patch against the official Morphe patches template.
The Morphe build needs GitHub Packages authentication, even for public packages.

1. Create a GitHub Personal Access Token (classic) with **read:packages** scope:
   https://github.com/settings/tokens/new?scopes=read:packages&description=Morphe
2. In this repository open **Settings → Secrets and variables → Actions**.
3. Add a repository secret named **MORPHE_GPR_TOKEN** with the token value.
4. Open **Actions → Build Samsung Dialer Morphe patch → Run workflow**,
   or push a change to the patch source.
5. When the workflow succeeds, open its run and download the
   `samsung-dialer-morphe-mpp` artifact.

Never paste the token into chat or commit it to the repository.

A successful compilation is not the same as successful runtime compatibility.
Apply the MPP to the exact Samsung Phone 15.9.72 APK and test on the device.
