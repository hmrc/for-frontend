/* overides jshint */
/*******************/
/* jshint -W079 */
/* jshint -W009 */
/* jshint -W098 */
/******************/

// set namespaces (remember to add new namespaces to .jshintrc)
var VORALD = {};
var VOCommon = {};
var VOFeedback = {};
var VOMessages = {};
var VOAlerts = {};
var VORadioToggle = {};
var ref;

(function ($) {
    'use strict';
    $(document).ready(function () {

        /** Init functions **/
        //voRALD.js
        VORALD.printLinkSetup();
        VORALD.printPageShouldPrintOnLoad();
        VORALD.addField();
        VORALD.removeField();
        VORALD.addFieldMulti();
        VORALD.removeFieldMulti();
        VORALD.selectMobile();
        VORALD.isEdit();
        VORALD.getReferrer();
        VORALD.addMultiButtonState();
        VORALD.doYouOwnTheProperty();
        VORALD.setHelpGDSClasses();

        //feedback.js
        VOFeedback.helpForm();

        //intelAlerts.js
        VOAlerts.intelAlert();

        //radioToggle.js
        VORadioToggle.toggleFieldsBasedOnCheckedRadioButton();

        //common.js
        VOCommon.anchorFocus();
        VOCommon.details();
        VOCommon.characterCount();
    });

})(jQuery);
