(function ($) {
    'use strict';

    VOAlerts.intelAlert = function () {

        //Section 9
        function intelCheckSection9(element){

            function intelCheckDate(element){
                var date1, date2;
                if($('[data-intel]').attr('data-intel')){
                    if($('#'+element+' input:eq(0)').val() && $('#'+element+' input:eq(1)').val()  && $('#'+element+' input:eq(2)').val() ){
                        date1 = $('#'+element+' input:eq(1)').val()+'/'+$('#'+element+' input:eq(0)').val()+'/'+$('#'+element+' input:eq(2)').val();
                        date2 = $('[data-intel]').attr('data-intel').split('/')[0].replace(/\s/g, '')+'/01/'+$('[data-intel]').attr('data-intel').split('/')[1].replace(/\s/g, '');
                    }
                }
                if (new Date(date1).getTime() < new Date(date2).getTime()) {
                    $('.'+element+'.intel-alert').removeClass('hidden');
                }else{
                    $('.'+element+'.intel-alert').addClass('hidden');
                }
            }
            function intelCheckDateChange(element){
                $('#'+element+' input').change(function(){
                    intelCheckDate(element);
                });
            }
            intelCheckDate(element);
            intelCheckDateChange(element);
        }

        function intelCheck(element){
            function intelCheckDate(element){
                var date1, date2;
                if($('[data-intel]').attr('data-intel')){
                    if($('#'+element+' input:eq(0)').val() && $('#'+element+' input:eq(1)').val()){
                        date1 = $('#'+element+' input:eq(0)').val()+'/01/'+$('#'+element+' input:eq(1)').val();
                        date2 = $('[data-intel]').attr('data-intel').split('/')[0].replace(/\s/g, '')+'/01/'+$('[data-intel]').attr('data-intel').split('/')[1].replace(/\s/g, '');
                    }
                }
                if (new Date(date1).getTime() < new Date(date2).getTime()) {
                    $('.'+element+'.intel-alert').removeClass('hidden');
                }else{
                    $('.'+element+'.intel-alert').addClass ('hidden');
                }
            }
            function intelCheckDateChange(element){
                $(document).on('change', '#'+element+' input', function (e) {
                    intelCheckDate(element);
                });
            }
            intelCheckDate(element);
            intelCheckDateChange(element);
        }

        //Section 9
        intelCheckSection9('rentBecomePayable');
        //Section 3
        intelCheck('firstOccupationDate');
        //Section 7
        intelCheck('rentReviewDetails_lastReviewDate');
        //Section 4
        intelCheck('sublet_rentFixedDate');
        //Section 10
        intelCheck('parking_annualSeparateParkingDate');
        //Section 13
        intelCheck('propertyAlterationsDetails_0_date');
        intelCheck('propertyAlterationsDetails_1_date');
        intelCheck('propertyAlterationsDetails_2_date');
        intelCheck('propertyAlterationsDetails_3_date');
        intelCheck('propertyAlterationsDetails_4_date');
        intelCheck('propertyAlterationsDetails_5_date');
        intelCheck('propertyAlterationsDetails_6_date');
        intelCheck('propertyAlterationsDetails_7_date');
        intelCheck('propertyAlterationsDetails_8_date');
        intelCheck('propertyAlterationsDetails_9_date');
    };
})(jQuery);
