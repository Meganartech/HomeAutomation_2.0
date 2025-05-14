import FAQFeedbackContent from '../../components/FAQFeedbackContent';
import SettingsLayout from '../../components/SettingsLayout';

export default function SettingsProfile() {
    return <SettingsLayout activeInsidePage="FAQ & Feedback" InsideContent={<FAQFeedbackContent />} />;
}