# User Data Management System  
*A modular Java framework for storing, managing, and persisting user data — with built-in autosave, crash recovery, and JSON serialization.*

---

### Overview

This project is a work-in-progress **lightweight in-memory database system** written in **pure Java**, featuring:

- Thread-safe user storage (via `ConcurrentSkipListMap`)
- utomatic JSON persistence with **Jackson**
- Background **auto-saving**
- Built-in **crash + shutdown safety hooks**
- Timestamped entries (creation time included)
- Dual-indexed lookups (by `UUID` or `Name`)
- Modular structure — `User`, `Database`, and `UserFileManager`

Perfect for:
- Small-scale games or simulation backends  
- Desktop applications needing user profiles  
- Quick prototypes that require persistence without full SQL overhead  

---
