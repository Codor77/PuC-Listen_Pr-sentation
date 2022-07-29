# Listen und deren Operationen
## Standardoperationen:
- Append
- GetSize
- GetValue
- RemoveAt
- InsertAt
- IsEmpty
- ListJoin

---

## Erweiterte Operationen
- Fold
- Map
- Filter

---

## Was war dazu nötig?
- Neue Tokens: [ ] zur Listenformatierung:
``` kotlin
object SLPAREN : Token()    // [
object SRPAREN : Token()    // ]
```

- Parser+Interpreter Erweiterungen für Listen und deren Operationen
