import MessageCenterContent from '../../components/MessageCenterContent';
import SettingsLayout from '../../components/SettingsLayout';

export default function SettingsProfile() {
    return <SettingsLayout activeInsidePage="Message Center" InsideContent={<MessageCenterContent />} />;
}