# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.3.0+based_on=1.7.0](https://github.com/CPardi/Messages-Mirror/releases/tag/0.3.0+based_on=1.7.0) - 2026-02-02

Based on version [1.7.0](https://github.com/FossifyOrg/Messages/releases/tag/1.7.0) of [FossifyOrg/Messages](https://github.com/FossifyOrg/Messages).

### Added

- Messages sent from a mirror are forwarded to the SMS host to send
- Messages sent from a host are shown on the mirror
- SMS send‑status are mirrored on other devices
- SMS deletions are mirrored on other devices
- Added validation for topic URLs, server toggle, and encryption keys to prevent misconfiguration.


### Changed


- Split topic text box into separate ones for topic and a custom ntfy server
- Re-arranged UI controls for easier setup


### Fixed

- QR Code UI elements now respect the current color scheme
- Resolved a timing issue that caused missed status updates
- Fixed exceptions when sending message while mirroring is disabled.

## [0.2.0+based_on=1.6.0](https://github.com/CPardi/Messages-Mirror/releases/tag/0.2.0+based_on=1.6.0) - 2025-12-09

Based on version [1.6.0](https://github.com/FossifyOrg/Messages/releases/tag/1.6.0) of [FossifyOrg/Messages](https://github.com/FossifyOrg/Messages).

### Changed

- Application ID has been updated


### Fixed

- Fix crash on exception from malformed messages
- Start Message Mirror service at boot
- Fix difficult to see icons in light mode
- Fix exceptions when changing color scheme

## [0.1.0+based_on=1.6.0](https://github.com/CPardi/Messages-Mirror/releases/tag/0.1.0+based_on=1.6.0) - 2025-11-29

Based on version [1.6.0](https://github.com/FossifyOrg/Messages/releases/tag/1.6.0) of [FossifyOrg/Messages](https://github.com/FossifyOrg/Messages).

### Added

- Added initial mirroring functionality for receiving SMS messages
- Added encrypted messages
