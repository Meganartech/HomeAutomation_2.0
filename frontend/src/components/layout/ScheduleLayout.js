import { FiUser, FiPlus, FiLogOut } from 'react-icons/fi';
import InsideLayout from './InsideLayout';

export default function ScheduleLayout({ activePage, InsideContent }) {
    const scheduleMenu = [
        { type: 'link', label: 'Your Schedule', path: '/schedule/your_schedule', icon: FiUser },
        { type: 'link', label: 'Create Schedule', path: '/schedule/create_schedule', icon: FiPlus },
        { type: 'divider' },
        { type: 'logout', icon: FiLogOut }
    ];

    return (
        <>
            <InsideLayout menu={scheduleMenu} activePage={'Schedule'} insideActivePage={activePage}>
                <InsideContent />
            </InsideLayout>
        </>
    );
};