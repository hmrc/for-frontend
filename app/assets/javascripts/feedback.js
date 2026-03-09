
    VOFeedback.helpForm = function() {
        const helpFormLink = $('#helpFormLink');

        if (helpFormLink.attr('href') !== undefined) {
            const linkUrl = helpFormLink.attr('href');
            const indexOfReferrerUrl = linkUrl.indexOf('&referrerUrl=');
            helpFormLink.attr('href', linkUrl.substring(0, indexOfReferrerUrl + 13) + window.location.href);
        }
    };
