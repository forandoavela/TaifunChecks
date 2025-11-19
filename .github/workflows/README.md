# GitHub Actions Workflows

This directory contains GitHub Actions workflows for building and releasing the FlightChecks Android app.

## Available Workflows

### 1. Android Build (`android-build.yml`)

**Triggers:**
- Push to `master`, `main`, or any `claude/**` branch
- Pull requests to `master` or `main`
- Manual trigger via GitHub Actions UI

**What it does:**
- Builds both debug and release APKs
- Uploads APKs as artifacts (available for 30 days)
- Runs on every push to track build health

**Artifacts:**
- `app-debug`: Debug APK ready for testing
- `app-release`: Signed release APK (if keystore configured) or unsigned

### 2. Release (`release.yml`)

**Triggers:**
- Push of version tags (e.g., `v1.0.0`, `v1.1.0`)

**What it does:**
- Builds production-ready APKs
- Creates a GitHub Release with release notes
- Attaches APKs to the release
- Keeps artifacts for 90 days

**How to create a release:**

```bash
# Create and push a version tag
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

The workflow will automatically:
1. Build the APKs
2. Create a GitHub Release
3. Attach the APKs to the release
4. Generate release notes

## Build Requirements

- **Java**: JDK 17 (Temurin distribution)
- **Gradle**: Uses Gradle wrapper (gradlew)
- **OS**: Ubuntu latest (Linux)

## Accessing Build Artifacts

### From Pull Requests / Commits:
1. Go to the "Actions" tab in GitHub
2. Click on the workflow run
3. Scroll to "Artifacts" section
4. Download the APK

### From Releases:
1. Go to the "Releases" section in GitHub
2. Click on the desired release
3. Download APKs from the "Assets" section

## APK Types

### Debug APK
- **Filename**: `app-debug.apk` or `FlightChecks-vX.X.X-debug.apk`
- **Use case**: Development and testing
- **Signing**: Signed with debug keystore
- **Installation**: Can be installed directly on any device with "Unknown sources" enabled

### Release APK (Signed)
- **Filename**: `app-release.apk` or `FlightChecks-vX.X.X-release.apk`
- **Use case**: Production builds
- **Signing**: Automatically signed with your keystore (if configured in GitHub Secrets)
- **Installation**: Can be installed directly on devices

## 🔐 Automatic Release Signing

The workflows now support **automatic signing** of release APKs using GitHub Secrets.

### Setup Required:

1. Generate a keystore (see [RELEASE_SIGNING_SETUP.md](../RELEASE_SIGNING_SETUP.md))
2. Add the following secrets to your GitHub repository:
   - `KEYSTORE_BASE64`: Your keystore file encoded in base64
   - `KEYSTORE_PASSWORD`: Password for the keystore
   - `KEY_ALIAS`: Alias of the signing key
   - `KEY_PASSWORD`: Password for the signing key

### How it Works:

- The workflow automatically decodes the keystore from the base64 secret
- Gradle uses environment variables to sign the release APK
- The signed APK is uploaded as an artifact
- **No manual signing needed!**

### Status Check:

After a build completes, check the build summary:
- ✅ `🔐 Release APK is signed and ready for distribution` - Signing successful
- ⚠️ `⚠️ Release APK is unsigned - configure GitHub Secrets to enable signing` - Secrets not configured

For detailed setup instructions, see [RELEASE_SIGNING_SETUP.md](../RELEASE_SIGNING_SETUP.md)

## Troubleshooting

### Build fails with "Permission denied"
The workflow automatically runs `chmod +x gradlew` to fix this.

### Gradle cache issues
The workflow uses `actions/setup-java@v4` with Gradle caching enabled. If issues persist, you can manually clear the cache in the Actions settings.

### Release APK not generated
Release builds may fail if ProGuard rules have issues. Check the workflow logs for details. The workflow uses `continue-on-error: true` for release builds to ensure debug builds always succeed.

## Manual Build (Local)

To build locally:

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease

# Find the APKs
ls -la app/build/outputs/apk/debug/
ls -la app/build/outputs/apk/release/
```

## Environment Variables

No environment variables or secrets are required for basic builds. For signed releases, you would need to configure:
- `KEYSTORE_FILE`: Base64 encoded keystore
- `KEYSTORE_PASSWORD`: Keystore password
- `KEY_ALIAS`: Key alias
- `KEY_PASSWORD`: Key password

(These are not currently configured in the workflows)
