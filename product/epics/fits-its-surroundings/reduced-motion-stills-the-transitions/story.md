---
id: reduced-motion-stills-the-transitions
epic: fits-its-surroundings
status: delivered
delivered: 2026-10-13T07:55:22Z
delivered_by: gate
context: portal
feature: appearance
title: Reduced motion stills the page transitions
depends_on: [CAT-02]
---

# Reduced motion stills the page transitions

## Story

As a visitor who asked my device for less motion I want the shop to change pages without any animation, so that
moving through the shop does not make me dizzy.

## Acceptance criteria

### Rule: Motion is switched off for a visitor who prefers reduced motion

#### reduced-motion-means-no-animation (happy path)
- Given a browser that supports animated transitions between pages and whose user prefers reduced motion
- When the visitor follows "View Details" from the catalogue to "Domain-Driven Design"
- Then the product page appears without any animation

## Changed expectations

- Under reduced motion the page content's fade and slide are off now, but the transition groups — the frame and
  the product image gliding from its card to its place — still run for 250 ms; after, no part of the page animates.

## Notes

- Both shops share the stylesheet and take this story alike, so that both hold the same spec
  (WP-107, decision `moving-between-pages-is-animated-01`).
