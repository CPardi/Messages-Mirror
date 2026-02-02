# Manual Testing Document

| Test Name          | Test Result | Notes |
|--------------------|-------------|-------|
| Device setup       |             |       |
| Receiving messages |             |       |
| Sending messages   |             |       |
| Deleting messages  |             |       |

---

## Device setup

Setup a first device as the host

1. Open "Settings"
2. Set "Device Mode" to "SMS Host"
3. Generate a random topic and encryption key
4. Click "Use another server" and paste local ntfy server 
5. Create subscription in ntfy
6. Go back and tap "Share Settings"

Then, a second device as the mirror

1. Open "Settings"
2. Set "Device Mode" to "Mirror"
3. Tap "Scan settings" and scan host QR code

Then ensure

[ ] Same settings on host and mirror

## Receiving messages

On the host

1. Open "Extended Control" -> Phone
2. Click "Send Message"

Then, check

[ ] Host has receive message
[ ] Mirror has receive message

Finally, perform the same test on the mirror.

## Sending messages

On the host

1. Open a conversation
2. Type a message and tap send

Then, check

- [ ] Host shows the sent message
  - [ ] Sent and delivered status are shown
- [ ] Mirror shows the sent message
  - [ ] Sent and delivered status are shown

Finally, perform the same test on the mirror.

## Deleting messages

On the host

1. Open a conversation
2. Delete one of the previous messages

Then, check

- [ ] Host no longer shows the deleted message
- [ ] Mirror no longer shows the deleted message

Finally, perform the same test on the mirror.
