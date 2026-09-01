config.set({
  browsers: ['ChromeHeadlessSandboxSafe'],
  customLaunchers: {
    ChromeHeadlessSandboxSafe: {
      base: 'ChromeHeadless',
      flags: [
        '--no-sandbox',
        '--disable-gpu',
        '--disable-gpu-compositing',
        '--disable-dev-shm-usage'
      ]
    }
  }
});
