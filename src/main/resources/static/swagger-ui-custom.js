(function () {
  // Monkey-patch fetch to detect sign-in responses and auto-authorize Swagger UI
  const originalFetch = window.fetch;
  window.fetch = function () {
    const args = arguments;
    return originalFetch.apply(this, args).then(async function (response) {
      try {
        // Determine request URL
        const req = args[0];
        const url = (typeof req === 'string') ? req : (req && req.url) ? req.url : '';
        if (url && url.indexOf('/api/v1/auth/sign-in') !== -1) {
          const authHeader = response.headers.get('Authorization') || response.headers.get('authorization');
          if (authHeader) {
            const token = authHeader.replace(/^Bearer\s+/i, '');
            // Build security object for Swagger UI
            const creds = { bearerAuth: { name: 'bearerAuth', schema: { type: 'http', scheme: 'bearer' }, value: 'Bearer ' + token } };
            try {
              if (window.ui && window.ui.authActions && typeof window.ui.authActions.authorize === 'function') {
                window.ui.authActions.authorize(creds);
                console.log('Swagger UI: auto-authorized with token from sign-in');
              } else if (window.ui && window.ui.getSystem && window.ui.getSystem().authActions) {
                window.ui.getSystem().authActions.authorize(creds);
                console.log('Swagger UI: auto-authorized (alternate)');
              } else {
                console.log('Swagger UI: ui.authActions not available yet');
              }
            } catch (err) {
              console.error('Swagger UI auto-auth error', err);
            }
          }
        }
      } catch (e) {
        console.error('Error in swagger-ui-custom fetch interceptor', e);
      }
      return response;
    });
  };
})();
