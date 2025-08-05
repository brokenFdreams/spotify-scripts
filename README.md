# Spotify Weekly New Releases Playlist

This Kotlin project uses the Spotify Web API to create a weekly playlist containing new releases (albums and singles) from artists you follow on Spotify. The playlist is automatically generated and updated with tracks released in the past 7 days.

## Features
- Retrieves the list of artists you follow on Spotify.
- Fetches new albums and singles released within the last week.
- Creates a public playlist with a name like `New Release - YYYY-MM-DD`.
- Handles pagination for large artist lists and albums.
- Can be run locally or automated via GitHub Actions.

## Prerequisites
- A Spotify account.
- Kotlin 2.2.0 and JVM 21.
- Gradle for building the project.
- A Spotify Developer Application (see [Setup](#setup)).

## Setup

### 1. Create a Spotify Developer Application
1. Go to the [Spotify Developer Dashboard](https://developer.spotify.com/dashboard).
2. Create a new application and note the following:
   - **Client ID**
   - **Client Secret**
3. Add `http://127.0.0.1:8888/callback` as a **Redirect URI** in the application settings.

### 2. Obtain a Refresh Token
1. Clone this repository:
   ```bash
   git clone https://github.com/your-username/spotify-weekly-new-releases.git
   cd spotify-weekly-new-releases
   ```
2. Set environment variables for your Spotify credentials in the IDEA's run configuration:
     ```
     SPOTIFY_CLIENT_ID="your_client_id"
     SPOTIFY_CLIENT_SECRET="your_client_secret"
     ```
3. Run the `GetRefreshToken` script to obtain a refresh token:
4. Open the provided URL in your browser, authorize the application, and copy the `code` from the redirect URL (e.g., `http://127.0.0.1:8888/callback?code=...`).
5. Paste the code into the terminal when prompted.
6. Save the displayed **Refresh Token** for use in the next steps.

### 3. Configure Environment Variables
For local execution or GitHub Actions, you’ll need the following environment variables:
- `SPOTIFY_CLIENT_ID`: Your Spotify application's Client ID.
- `SPOTIFY_CLIENT_SECRET`: Your Spotify application's Client Secret.
- `SPOTIFY_REFRESH_TOKEN`: The refresh token obtained above.
- `SPOTIFY_USERNAME`: Your Spotify user ID (found in your Spotify profile, e.g., `spotify:user:your_user_id`).

## Running Locally
1. Ensure the environment variables are set (see above).
2. Run the main script to create the playlist:
   ```bash
   ./gradlew runNewReleases
   ```
3. The script will:
   - Fetch your followed artists.
   - Retrieve new releases from the past 7 days.
   - Create a new public playlist with the tracks.
   - Log the playlist URL and added tracks.

## Automating with GitHub Actions
To run the script weekly on GitHub Actions:

1. **Fork this repository** to your GitHub account.
2. **Add secrets** in your GitHub repository:
   - Go to `Settings > Secrets and variables > Actions > Secrets`.
   - Add the following secrets:
     - `SPOTIFY_CLIENT_ID`
     - `SPOTIFY_CLIENT_SECRET`
     - `SPOTIFY_REFRESH_TOKEN`
     - `SPOTIFY_USERNAME`
3. **Enable the GitHub Actions workflow**:
   - The `.github/workflows/spotify-script.yaml` file is configured to run the script every Monday at midnight (UTC).
   - You can also trigger it manually via the GitHub Actions tab.

## Project Structure
- `GetRefreshToken.kt`: Script to obtain a Spotify refresh token.
- `CustomReleaseRadar.kt`: Main script to fetch new releases and create a playlist.
