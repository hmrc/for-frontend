/* overides jshint */
/*******************/
/* jshint -W079 */
/* jshint -W009 */
/* jshint -W098 */
/******************/

// set namespaces (remember to add new namespaces to .jshintrc)
var VoaFor = {};
var VoaCommon = {};
var VoaFeedback = {};
var VoaMessages = {};
var VoaAlerts = {};
var VoaPostCode = {};
var VoaRadioToggle = {};
var ref;

(function ($) {
    'use strict';
    $(document).ready(function () {

        /** Init functions **/

        //voaFor.js
        VoaFor.errorFocus();
        VoaFor.addressAbroad();
        VoaFor.addField();
        VoaFor.removeField();
        VoaFor.addFieldMulti();
        VoaFor.removeFieldMulti();
        VoaFor.selectMobile();
        VoaFor.rentLength();
        VoaFor.updateLabelToggle();
        VoaFor.isEdit();
        VoaFor.radioAgreement();
        VoaFor.excludeVat();
        VoaFor.agreementType();
        VoaFor.populateLettingsAddress();
        VoaFor.getReferrer();
        VoaFor.formatPostcode();
        VoaFor.toggleAgentLeaseContainsRentReviews();
        VoaFor.toggleImage();
        VoaFor.toggleYearsMonths();
        VoaFor.addMultiButtonState();

        //feedback.js
        VoaFeedback.feedbackOverrides();
        VoaFeedback.toggleHelp();
        VoaFeedback.helpForm();

        //intelAlerts.js
        VoaAlerts.intelAlert();

        //postcodeLookup.js
        VoaPostCode.postcodeLookup();
        VoaPostCode.postcodeLookupElements();

        //radioToggle.js
        VoaRadioToggle.radioDataShowField();
        VoaRadioToggle.radioDataShowFields();
        
        //common.js
        VoaCommon.GdsSelectionButtons();
        VoaCommon.linkShowManualAddress();
        VoaCommon.smoothScrollAndFocus();
        VoaCommon.addAnchors();
        VoaCommon.addErrorAnchors();
        VoaCommon.anchorFocus();
        VoaCommon.details();
        VoaCommon.characterCount();
        VoaCommon.stickyFooter();

        
    });

})(jQuery);