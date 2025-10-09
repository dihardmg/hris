# K6 Login Load Testing

Simple K6 load testing for HRIS login API endpoint.

## 🚀 Quick Start

### 1. Install k6
```bash
# macOS
brew install k6

# Linux
sudo apt-get install k6

# Windows (with Chocolatey)
choco install k6
```

### 2. Run Tests

**Quick Test (30 seconds):**
```bash
make test-quick
```

**Normal Test (2 minutes):**
```bash
make test-normal
```

**Stress Test (up to 150 VUs):**
```bash
make test-stress
```

**All Tests:**
```bash
make test-all
```

## 🔧 Configuration

Change credentials:
```bash
make test-normal EMAIL=your@email.com PASSWORD=yourpassword
```

Change server:
```bash
make test-normal BASE_URL=https://api.hris.com
```

## 📊 Test Scenarios

| Test | Duration | VUs | Purpose |
|------|----------|-----|---------|
| Quick | 30s | 5 | Fast validation |
| Normal | 2m | 10 | Standard load |
| Stress | 2.5m | 50-150 | Find breaking point |

## 📈 Expected Results

- **Status**: 200 OK
- **Response**: Should contain JWT token
- **Response Time**: < 1s (95th percentile)
- **Success Rate**: > 90%

## 🐛 Troubleshooting

**Connection refused:**
```bash
make check-api  # Check if HRIS is running
```

**Authentication failed:**
- Verify email and password are correct
- Check if user account is active

**Rate limiting:**
- Test may hit rate limits after many requests
- Wait a few minutes and retry

## 📞 Help

```bash
make help  # Show all commands
```